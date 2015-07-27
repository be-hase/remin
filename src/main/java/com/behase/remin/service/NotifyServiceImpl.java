package com.behase.remin.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.behase.remin.model.Group;
import com.behase.remin.model.Node;
import com.behase.remin.model.Notice;
import com.behase.remin.model.NoticeItem;
import com.behase.remin.model.NoticeJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {
	@Value("${relumin.host}")
	private String reluminHost;

	@Value("${server.port}")
	private String serverPort;

	@Value("${notice.mail.from}")
	private String noticeMailFrom;

	@Autowired
	private MailSender mailSender;

	@Autowired
	private ObjectMapper mapper;

	private ExecutorService senderService = Executors.newFixedThreadPool(10);

	@Override
	public void notify(final Group group, final Notice notice, final List<NoticeJob> jobs) {
		senderService.execute(new Runnable() {
			@Override
			public void run() {
				String from = StringUtils.isNotBlank(notice.getMail().getFrom()) ? notice.getMail().getFrom()
					: noticeMailFrom;
				if (mailSender != null && StringUtils.isNotBlank(from)) {
					try {
						notifyByMail(group, notice, jobs);
					} catch (Exception e) {
						log.error("Failed to send mail.", e);
					}
				}
				if (StringUtils.isNotBlank(notice.getHttp().getUrl())) {
					try {
						notifyByHttp(group, notice, jobs);
					} catch (Exception e) {
						log.error("Failed to send Http.", e);
					}
				}
			}

			private void notifyByMail(final Group group, final Notice notice, final List<NoticeJob> jobs) {
				String lineSeparator = System.lineSeparator();

				StringBuilder text = new StringBuilder();
				text.append(String.format("Group name : %s%s", group.getGroupName(), lineSeparator));
				if (StringUtils.isNotBlank(reluminHost)) {
					text.append(String.format("Check this page : http://%s:%s/#/group/%s%s", reluminHost, serverPort, group.getGroupName(), lineSeparator));
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

				String from = StringUtils.isNotBlank(notice.getMail().getFrom()) ? notice.getMail().getFrom()
					: noticeMailFrom;
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(StringUtils.trim(from));
				message.setTo(Splitter.on(",").trimResults().splitToList(notice.getMail().getTo()).stream().map(v -> StringUtils.trim(v)).toArray(size -> new String[size]));
				message.setSubject(String.format("[Remin] Notification of \"%s\". Please check.", group.getGroupName()));
				message.setText(text.toString());

				log.info("Send mail. from={}, to={}", message.getFrom(), message.getTo());
				mailSender.send(message);
			}

			private void notifyByHttp(final Group group, final Notice notice, final List<NoticeJob> jobs)
					throws Exception {
				String data = mapper.writeValueAsString(jobs);

				Form form = Form.form().add("groupName", group.getGroupName()).add("data", data);
				if (notice.isNotifyWhenDisconnected()) {
					List<String> disconnectedHostAndPorts = group.getNodes().stream().filter(node -> !node.isConnected()).map(Node::getHostAndPort).collect(Collectors.toList());
					form.add("disconnectedHostAndPorts", Joiner.on(",").join(disconnectedHostAndPorts));
				}

				Request.Post(notice.getHttp().getUrl()).connectTimeout(3000).socketTimeout(3000).bodyForm(form.build()).execute();
			}
		});
	}
}
