package test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import common.ApplicationConfiguration;
import enums.AccessTool;
import service.impl.SeleniumRobotsService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={ApplicationConfiguration.class})
public class TestMain {

	@Autowired
	private SeleniumRobotsService service;
	
	@Test
	public void test() {
		assertTrue(service.run(AccessTool.GOOGLE));
	}
	
}
