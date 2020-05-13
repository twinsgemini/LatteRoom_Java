package network.server.test;

import java.util.Collection;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.log4j.BasicConfigurator;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class TestQuartz {

	public static void main(String[] args) {
		
		/*	[quartz-2.3.0-distribution]
		 *   need to import this
		 * 		- quartz-2.3.0-SNAPSHOT.jar
		 *		- slf4j-api-1.7.7.jar
		 *		- log4j-1.2.16.jar
		 */
		
		BasicConfigurator.configure();
		
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
		
		try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            
            JobDetail job = newJob(TestJob.class)
                .withIdentity("jobName", Scheduler.DEFAULT_GROUP)
                .build();
            
            Trigger trigger = newTrigger()
                .withIdentity("trggerName", Scheduler.DEFAULT_GROUP)
                .startNow()
                .withSchedule(cronSchedule("0/5 * * * * ?"))
                .build();
                        
            scheduler.scheduleJob(job, trigger);
        } catch(Exception e) {
            e.printStackTrace();
        }
	}

}
