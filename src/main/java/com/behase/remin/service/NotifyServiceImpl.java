package com.behase.remin.service;

import com.behase.remin.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {
    @Value("${remin.host}")
    private String reminHost;

    @Value("${server.port}")
    private String serverPort;

    @Value("${notice.mail.from}")
    private String noticeMailFrom;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private ObjectMapper mapper;

    private ExecutorService senderService = Executors.newFixedThreadPool(10);

    @PreDestroy
    public void preDestroy() {
        if (senderService != null) {
            senderService.shutdown();
            try {
                if (senderService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                    senderService.shutdownNow();
                }
            } catch (InterruptedException e) {
                senderService.shutdownNow();
            }
        }
    }

    @Override
    public void notify(final Group group, final Notice notice, final List<NoticeJob> jobs) {
        senderService.execute(() -> {
            try {
                notifyByMail(group, notice, jobs);
            } catch (Exception e) {
                log.error("Failed to send mail.", e);
            }
            try {
                notifyByHttp(group, notice, jobs);
            } catch (Exception e) {
                log.error("Failed to send Http.", e);
            }
        });
    }

    void notifyByMail(Group group, Notice notice, List<NoticeJob> jobs) {
        if (mailSender == null) {
            log.info("Ignore mail notice. Because no mail setting. groupName={}", group.getGroupName());
            return;
        }

        String from = StringUtils.trim(StringUtils.defaultIfBlank(notice.getMail().getFrom(), noticeMailFrom));
        List<String> toList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(notice.getMail().getTo());
        String[] to = toList.toArray(new String[toList.size()]);

        if (StringUtils.isBlank(from)) {
            log.info("Ignore mail notice. Because mo mail-from setting. groupName={}", group.getGroupName());
            return;
        }
        if (to.length == 0) {
            log.info("Ignore mail notice. Because no mail-to setting. groupName={}", group.getGroupName());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(String.format("[Remin] Notification of \"%s\". Please check.", group.getGroupName()));
        message.setText(getSummaryText(group, notice, jobs));

        log.info("Send mail. from={}, to={}", message.getFrom(), message.getTo());
        mailSender.send(message);
    }

    void notifyByHttp(Group group, Notice notice, List<NoticeJob> jobs)
            throws Exception {
        String httpUrl = notice.getHttp().getUrl();

        if (StringUtils.isBlank(httpUrl)) {
            log.info("Ignore http notice. Because no http-url setting. groupName={}", group.getGroupName());
            return;
        }

        Map<String, Object> body = Maps.newLinkedHashMap();
        body.put("groupName", group.getGroupName());
        body.put("data", jobs);
        body.put("summaryText", getSummaryText(group, notice, jobs));
        if (notice.isNotifyWhenDisconnected()) {
            List<String> disconnectedHostAndPorts = group.getNodes().stream().filter(node -> !node.isConnected()).map(Node::getHostAndPort).collect(Collectors.toList());
            body.put("disconnectedHostAndPorts", Joiner.on(",").join(disconnectedHostAndPorts));
        }

        Request.Post(httpUrl)
                .connectTimeout(3000)
                .socketTimeout(3000)
                .bodyString(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON)
                .execute();
    }

    String getSummaryText(Group group, Notice notice, List<NoticeJob> jobs) {
        StringBuilder text = new StringBuilder();
        String lineSeparator = System.lineSeparator();

        text.append(String.format("Group name : %s%s", group.getGroupName(), lineSeparator));
        if (StringUtils.isNotBlank(reminHost)) {
            text.append(String.format("Check this page : http://%s:%s/#/group/%s%s", reminHost, serverPort, group.getGroupName(), lineSeparator));
        }
        text.append(String.format("%s", lineSeparator));

        if (notice.isNotifyWhenDisconnected()) {
            List<String> disconnectedHostAndPorts = group.getNodes().stream().filter(node -> !node.isConnected()).map(Node::getHostAndPort).collect(Collectors.toList());
            if (!disconnectedHostAndPorts.isEmpty()) {
                text.append(String.format("There are disconnected hostAndPorts.%s", lineSeparator));
                text.append(String.format("%s%s", Joiner.on(", ").join(disconnectedHostAndPorts), lineSeparator));
                text.append(String.format("%s", lineSeparator));
            }
        }

        int i = 0;
        for (NoticeJob job : jobs) {
            i++;
            text.append(String.format("#%s.%s", i, lineSeparator));
            text.append(String.format("Metrics name : %s%s", job.getItem().getMetricsName(), lineSeparator));
            text.append(String.format("Notification condition : %s %s %s%s",
                    job.getItem().getMetricsName(),
                    NoticeItem.NoticeOperator.getNoticeOperator(job.getItem().getOperator()).getLabel(),
                    NoticeItem.NoticeValueType.STRING.getValue().equals(job.getItem().getValueType()) ? "\""
                            + job.getItem().getValue() + "\"" : job.getItem().getValue(),
                    lineSeparator));
            text.append(String.format("Result values : %s", lineSeparator));
            for (NoticeJob.ResultValue resultValue : job.getResultValues()) {
                text.append(String.format("    %s (on %s)%s", resultValue.getValue(), resultValue.getHostAndPort(), lineSeparator));
            }

            text.append(String.format("%s", lineSeparator));
        }

        return text.toString();
    }
}
