package com.tuling.tim.client.client;

import com.tuling.tim.client.config.AppConfiguration;
import com.tuling.tim.client.init.TIMClientHandleInitializer;
import com.tuling.tim.client.service.EchoService;
import com.tuling.tim.client.service.MsgHandle;
import com.tuling.tim.client.service.ReConnectManager;
import com.tuling.tim.client.service.RouteRequest;
import com.tuling.tim.client.service.impl.ClientInfo;
import com.tuling.tim.client.thread.ContextHolder;
import com.tuling.tim.client.vo.req.GoogleProtocolVO;
import com.tuling.tim.client.vo.req.LoginReqVO;
import com.tuling.tim.client.vo.res.TIMServerResVO;
import com.tuling.tim.common.constant.Constants;
import com.tuling.tim.common.protocol.TIMReqMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @since JDK 1.8
 */
@Component
public class TIMClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(TIMClient.class);

    private EventLoopGroup group = new NioEventLoopGroup(1, new DefaultThreadFactory("tim-work"));

    @Value("${tim.user.id}")
    private long userId;

    @Value("${tim.user.userName}")
    private String userName;

    private SocketChannel channel;

    @Autowired
    private EchoService echoService;

    @Autowired
    private RouteRequest routeRequest;

    @Autowired
    private AppConfiguration configuration;

    @Autowired
    private MsgHandle msgHandle;

    @Autowired
    private ClientInfo clientInfo;

    @Autowired
    private ReConnectManager reConnectManager;

    /**
     * ????????????
     */
    private int errorCount;

    //???spring????????????????????????
    @PostConstruct
    public void start() throws Exception {

        //?????? + ?????????????????????????????? ip+port
        TIMServerResVO.ServerInfo timServer = userLogin();

        //???????????????,??????????????????
        startClient(timServer);

        //????????????????????????????????????ID???channel????????????
        loginTIMServer();
    }

    /**
     * ?????????????????????netty????????????????????????
     *
     * @param timServer
     * @throws Exception
     */
    private void startClient(TIMServerResVO.ServerInfo timServer) {
        //??????netty?????????????????????
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new TIMClientHandleInitializer());

        ChannelFuture future = null;
        try {
            future = bootstrap.connect(timServer.getIp(), timServer.getTimServerPort()).sync();
        } catch (Exception e) {
            errorCount++;

            if (errorCount >= configuration.getErrorCount()) {
                LOGGER.error("??????????????????????????????[{}]???", errorCount);
                msgHandle.shutdown();
            }
            LOGGER.error("Connect fail!", e);
        }
        if (future.isSuccess()) {
            echoService.echo("Start tim client success!");
            LOGGER.info("?????? tim client ??????");
        }
        channel = (SocketChannel) future.channel();
    }

    /**
     * ??????+???????????????
     *
     * @return ?????????????????????
     * @throws Exception
     */
    private TIMServerResVO.ServerInfo userLogin() {
        LoginReqVO loginReqVO = new LoginReqVO(userId, userName);
        TIMServerResVO.ServerInfo timServer = null;
        try {
            timServer = routeRequest.getTIMServer(loginReqVO);

            //??????????????????
            clientInfo.saveServiceInfo(timServer.getIp() + ":" + timServer.getTimServerPort())
                    .saveUserInfo(userId, userName);

            LOGGER.info("timServer=[{}]", timServer.toString());
        } catch (Exception e) {
            errorCount++;

            if (errorCount >= configuration.getErrorCount()) {
                echoService.echo("The maximum number of reconnections has been reached[{}]times, close tim client!", errorCount);
                msgHandle.shutdown();
            }
            LOGGER.error("login fail", e);
        }
        return timServer;
    }

    /**
     * ??????????????????
     */
    private void loginTIMServer() {
        TIMReqMsg login = new TIMReqMsg(userId, userName, Constants.CommandType.LOGIN);
        ChannelFuture future = channel.writeAndFlush(login);
        future.addListener((ChannelFutureListener) channelFuture ->
                echoService.echo("Registry tim server success!")
        );
    }

    /**
     * ?????????????????????
     *
     * @param msg
     */
    public void sendStringMsg(String msg) {
        ByteBuf message = Unpooled.buffer(msg.getBytes().length);
        message.writeBytes(msg.getBytes());
        ChannelFuture future = channel.writeAndFlush(message);
        future.addListener((ChannelFutureListener) channelFuture ->
                LOGGER.info("??????????????????????????????={}", msg));

    }

    /**
     * ?????? Google Protocol ??????????????????
     *
     * @param googleProtocolVO
     */
    public void sendGoogleProtocolMsg(GoogleProtocolVO googleProtocolVO) {

        TIMReqMsg protocol = new TIMReqMsg(googleProtocolVO.getRequestId(), googleProtocolVO.getMsg(), Constants.CommandType.MSG);
        ChannelFuture future = channel.writeAndFlush(protocol);
        future.addListener((ChannelFutureListener) channelFuture ->
                LOGGER.info("????????????????????? Google Protocol ??????={}", googleProtocolVO.toString()));

    }


    /**
     * 1. clear route information.
     * 2. reconnect.
     * 3. shutdown reconnect job.
     * 4. reset reconnect state.
     *
     * @throws Exception
     */
    public void reconnect() throws Exception {
        if (channel != null && channel.isActive()) {
            return;
        }
        //?????????????????????????????????
        routeRequest.offLine();

        echoService.echo("tim server shutdown, reconnecting....");
        //?????????????????????start()?????????
        start();
        echoService.echo("Great! reConnect success!!!");
        reConnectManager.reConnectSuccess();
        ContextHolder.clear();
    }

    /**
     * ??????
     *
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        if (channel != null) {
            channel.close();
        }
    }
}
