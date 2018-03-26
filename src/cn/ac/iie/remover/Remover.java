package cn.ac.iie.remover;

import cn.ac.iie.remover.config.ConfLoading;
import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.config.ServiceMode;
import cn.ac.iie.remover.entity.ReceiveFileEntity;
import cn.ac.iie.remover.entity.SendFileEntity;
import cn.ac.iie.remover.receive.*;
import cn.ac.iie.remover.send.CompressZipHandler;
import cn.ac.iie.remover.send.DeleteSendDataHandler;
import cn.ac.iie.remover.send.SendDataGet;
import cn.ac.iie.remover.send.ToHDFSHandler;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Remover {
    private static Logger log = LoggerFactory.getLogger(Remover.class);

    public static Disruptor<SendFileEntity> disruptorSend = null;
    public static Disruptor<ReceiveFileEntity> disruptorReceive = null;

    public static void main(String[] args) {
        try {
            PropertyConfigurator.configure(System.getProperty("log4j.configuration"));
            log.info("Remover starting....");
            ConfLoading.init(Config.class, System.getProperty("config"));

            switch (Config.serviceMode) {
                case ServiceMode.SEND:
                    send();
                    break;
                case ServiceMode.RECEIVE:
                    receive();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void send() {

        disruptorSend = new Disruptor<SendFileEntity>(SendFileEntity::new, Config.czipThreadNumber * 2, DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI, new BlockingWaitStrategy());

        CompressZipHandler[] czipHandler = new CompressZipHandler[Config.czipThreadNumber];
        for (int i = 0; i < czipHandler.length; i++) {
            czipHandler[i] = new CompressZipHandler();
        }

        disruptorSend
                .handleEventsWithWorkerPool(czipHandler)
                .then(new ToHDFSHandler())
                .then(new DeleteSendDataHandler());

        RingBuffer<SendFileEntity> ringBuffer = disruptorSend.start();

        Thread thread = new Thread(new SendDataGet(ringBuffer), "SendDataThread");
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Execute shutdown Hook.....");
            log.info("RingBuffer size:{};", ringBuffer.getBufferSize() - ringBuffer.remainingCapacity());
            SendDataGet.isSend = false;
            disruptorSend.shutdown();
            log.info("Remover exiting......");
        }));
    }

    @SuppressWarnings("unchecked")
    private static void receive() {

        disruptorReceive = new Disruptor<ReceiveFileEntity>(ReceiveFileEntity::new, Config.dczipThreadNumber * 2, DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI, new BlockingWaitStrategy());

        DecompressZipHandler[] dczipHandler = new DecompressZipHandler[Config.dczipThreadNumber];
        for (int i = 0; i < dczipHandler.length; i++) {
            dczipHandler[i] = new DecompressZipHandler();
        }

        disruptorReceive
                .handleEventsWith(new ToLoongStoreHandler())
                .thenHandleEventsWithWorkerPool(dczipHandler)
                .then(new PutHDFSHandler())
                .then(new DeleteReceiveDataHandler());

        RingBuffer<ReceiveFileEntity> ringBuffer = disruptorReceive.start();

        Thread thread = new Thread(new ReceiveDataGet(ringBuffer), "ReceiveDataThread");
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Execute shutdown Hook.....");
            log.info("RingBuffer size:{};", ringBuffer.getBufferSize() - ringBuffer.remainingCapacity());
            ReceiveDataGet.isReceive = false;
            disruptorReceive.shutdown();
            log.info("Remover exiting......");
        }));
    }

}
