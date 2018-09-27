/*
 * Copyright (c) 2017 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.tas.dxc.role;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 *
 * @author banra06@ca.com katra03@ca.com
 */

public class KafkaZookeeperRole extends AbstractRole {

	private String installDir;
	private ITasResolver tasResolver;
	public static final String KAFKA_START = "kafkaStart";
	public static final String CREATE_TOPIC = "createTopic";
	public static final String KAFKA_STOP = "kafkaStop";
	public static final String ZOOKEEPER_START = "zookeeperStart";
	public static final String ZOOKEEPER_STOP = "zookeeperStop";
    
    private final RunCommandFlowContext startKafkaFlowContext;
    private final RunCommandFlowContext stopKafkaFlowContext;
    private final RunCommandFlowContext startZookeeperFlowContext;
    private final RunCommandFlowContext stopZookeeperFlowContext;
    private final RunCommandFlowContext createTopicsFlowContext;

	protected KafkaZookeeperRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
        this.installDir = builder.installDir;
        this.startKafkaFlowContext = builder.startKafkaFlowContext;
        this.stopKafkaFlowContext = builder.stopKafkaFlowContext;
        this.startZookeeperFlowContext = builder.startZookeeperFlowContext;
        this.stopZookeeperFlowContext = builder.stopZookeeperFlowContext;
        this.createTopicsFlowContext = builder.createTopicsFlowContext;

	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		getArtifacts(aaClient);
		KafkaDeploy(aaClient);
		createKafkaZookeeper(aaClient);
		createTopicsScript(aaClient);

	}

	public RunCommandFlowContext getStartKafkaFlowContext() {
        return startKafkaFlowContext;
    }

    public RunCommandFlowContext getStopKafkaFlowContext() {
        return stopKafkaFlowContext;
    }

    public RunCommandFlowContext getStartZookeeperFlowContext() {
        return startZookeeperFlowContext;
    }

    public RunCommandFlowContext getStopZookeeperFlowContext() {
        return stopZookeeperFlowContext;
    }

    public RunCommandFlowContext getCreateTopicsFlowContext() {
        return createTopicsFlowContext;
    }

    private void createKafkaZookeeper(IAutomationAgentClient aaClient) {

		Collection<String> data = Arrays
				.asList("echo Started",
						"cd "
								+ installDir
								+ "bin && ./zookeeper-server-start.sh ../config/zookeeper.properties");

		FileModifierFlowContext createZookeeperStart = new FileModifierFlowContext.Builder()
				.create(installDir + "startZookeeper.sh", data).build();

		runFlow(aaClient, FileModifierFlow.class, createZookeeperStart);

		Collection<String> data1 = Arrays.asList("echo Started", "cd "
				+ installDir
				+ "bin && ./kafka-server-start.sh ../config/server.properties");

		FileModifierFlowContext createKafkaStart = new FileModifierFlowContext.Builder()
				.create(installDir + "/startkafka.sh", data1).build();

		runFlow(aaClient, FileModifierFlow.class, createKafkaStart);

		Collection<String> data2 = Arrays
				.asList("kill -9 `ps -ef|grep config/zookeeper.properties|grep -v grep|awk '{print $2}'`");

		FileModifierFlowContext createZookeeperStop = new FileModifierFlowContext.Builder()
				.create(installDir + "/stopZookeeper.sh", data2).build();

		runFlow(aaClient, FileModifierFlow.class, createZookeeperStop);

		Collection<String> data3 = Arrays
				.asList("kill -9 `ps -ef|grep config/server.properties|grep -v grep|awk '{print $2}'`");

		FileModifierFlowContext createKafkaStop = new FileModifierFlowContext.Builder()
				.create(installDir + "/stopkafka.sh", data3).build();

		runFlow(aaClient, FileModifierFlow.class, createKafkaStop);

		Map<String, String> updateArgs = new HashMap<String, String>();
		updateArgs.put("log.retention.hours=(.*)", "log.retention.hours=1");

		updateArgs.put("#log.retention.bytes=(.*)",
				"log.retention.bytes=100000000");

		FileModifierFlowContext updateProps = new FileModifierFlowContext.Builder()
				.replace(installDir + "config/server.properties", updateArgs)
				.build();
		runFlow(aaClient, FileModifierFlow.class, updateProps);
	}

	private void createTopicsScript(IAutomationAgentClient aaClient) {

		Collection<String> data = Arrays
				.asList("bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 100 --topic analytics",
						"bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 100 --topic maaAggregator",
						"bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 100 --topic maaReadServer",
						"bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 100 --topic mdoCrashQueueName",
						"bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 100 --topic maaBAAggregator",
						"bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 100 --topic dxc",
						"bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic baProfiles --config cleanup.policy=compact");

		FileModifierFlowContext createTopicSH = new FileModifierFlowContext.Builder()
				.create(installDir + "createtopics.sh", data).build();

		runFlow(aaClient, FileModifierFlow.class, createTopicSH);
	}

	private void getArtifacts(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm",
				"ExperienceCollectorBundle", "tar.gz",
				DXCRole.DXC_VERSION));
		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination(installDir).build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);

	}

	private void KafkaDeploy(IAutomationAgentClient aaClient) {
        Collection<String> data =
            Arrays.asList("(tar -xvf " + installDir + "DigitalExperienceCollector* -C "
                + installDir + ") &&" + " ( mv -f " + installDir
                + "DigitalExperienceCollector*tar.gz /tmp) &&" + " ( mv " + installDir
                + "DigitalExperienceCollector*/kafka/* " + installDir + ") &&" + " ( rm -rf "
                + installDir + "DigitalExperienceCollector* ) &&" + " ( tar -xvf " + installDir
                + "kafka*tgz -C " + installDir + ") &&" + " ( mv -v " + installDir + "kafka*/* "
                + installDir + ") &&" + " ( rm -rf " + installDir + "kafka*)");
		FileModifierFlowContext makeFileModification = new FileModifierFlowContext.Builder()
				.create("/opt/movekafkaFiles.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, makeFileModification);
		RunCommandFlowContext command = new RunCommandFlowContext.Builder(
				"movekafkaFiles.sh").workDir("/opt").build();
		runCommandFlow(aaClient, command);

	}

	public static class LinuxBuilder extends Builder {

		public LinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
		}
	}

	public static class Builder extends
			BuilderBase<Builder, KafkaZookeeperRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		private String installDir = "/opt/kafka/";
		
        private RunCommandFlowContext startKafkaFlowContext;
        private RunCommandFlowContext stopKafkaFlowContext;
        private RunCommandFlowContext startZookeeperFlowContext;
        private RunCommandFlowContext stopZookeeperFlowContext;
        private RunCommandFlowContext createTopicsFlowContext;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public KafkaZookeeperRole build() {
			startKafka();
			stopKafka();
			startZookeeper();
			stopZookeeper();
			createTopics();
			return getInstance();
		}

		@Override
		protected KafkaZookeeperRole getInstance() {
			return new KafkaZookeeperRole(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		private void startKafka() {
		    startKafkaFlowContext = new RunCommandFlowContext.Builder(
					"startkafka.sh").workDir(installDir)
					.terminateOnMatch("Started").build();
			getEnvProperties().add(KAFKA_START, startKafkaFlowContext);

		}

		private void stopKafka() {
		    stopKafkaFlowContext = new RunCommandFlowContext.Builder(
					"stopkafka.sh").workDir(installDir).build();
			getEnvProperties().add(KAFKA_STOP, stopKafkaFlowContext);

		}

		private void startZookeeper() {
		    startZookeeperFlowContext = new RunCommandFlowContext.Builder(
					"startZookeeper.sh").workDir(installDir)
					.terminateOnMatch("Started").build();
			getEnvProperties().add(ZOOKEEPER_START, startZookeeperFlowContext);

		}

		private void stopZookeeper() {
		    stopZookeeperFlowContext = new RunCommandFlowContext.Builder(
					"stopZookeeper.sh").workDir(installDir).build();
			getEnvProperties().add(ZOOKEEPER_STOP, stopZookeeperFlowContext);

		}

		private void createTopics() {
		    createTopicsFlowContext = new RunCommandFlowContext.Builder(
					"createtopics.sh").workDir(installDir).build();
			getEnvProperties().add(CREATE_TOPIC, createTopicsFlowContext);

		}

	}

}
