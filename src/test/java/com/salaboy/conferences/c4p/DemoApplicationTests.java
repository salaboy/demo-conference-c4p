package com.salaboy.conferences.c4p;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {


	private static final String OUTPUT_TOPIC = "test-data";

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, OUTPUT_TOPIC);

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getEmbeddedKafka().getBrokersAsString());
	}


	@Test
	public void contextLoads() {

	}



}
