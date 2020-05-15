package network.server.dao;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import arduino.device.vo.Alert;

public class AlertScheduler {
	
	private String settedTime;
	private JobDetail job;
	private Trigger trigger;
	
	private SchedulerFactory factory = new StdSchedulerFactory();
	private Scheduler scheduler;
	
	public AlertScheduler() {
		try {
			scheduler = factory.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		
	}
	
	public void set(Alert alert) {
		
		settedTime = "0 " + alert.getMin() + " " + alert.getHour() + 
				" ? * " + alert.getWeeks();
		
//		BasicConfigurator.configure();
		
		job = newJob(JobExecutor.class)
                .withIdentity("job", Scheduler.DEFAULT_GROUP)
                .build();
		trigger = newTrigger()
                .withIdentity("trigger", Scheduler.DEFAULT_GROUP)
                .startNow()
                .withSchedule(cronSchedule(settedTime))
                .build();
		
		try {
			scheduler.clear();
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	
	}
	
}

