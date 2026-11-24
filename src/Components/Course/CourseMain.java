/**
 * Copyright(c) 2021 All rights reserved by Jungho Kim in MyungJi University 
 */
package Components.Course;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import Framework.Event;
import Framework.EventId;
import Framework.EventQueue;
import Framework.RMIEventBus;

public class CourseMain {
	public static void main(String[] args) throws FileNotFoundException, IOException, NotBoundException {
		CourseComponent coursesList = new CourseComponent("Courses.txt");
		RMIEventBus eventBus = (RMIEventBus) Naming.lookup("EventBus");
		long componentId = eventBus.register();
		System.out.println("CourseMain (ID:" + componentId + ") is successfully registered...");

		boolean done = false;
		while (!done) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			EventQueue eventQueue = eventBus.getEventQueue(componentId);
			while (!done && eventQueue.getSize() > 0) {
				Event event = eventQueue.getEvent();
				switch (event.getEventId()) {
				case ListCourses:
					printLogEvent("Get", event);
					eventBus.sendEvent(new Event(EventId.ClientOutput, makeCourseList(coursesList)));
					break;
				case RegisterCourses:
					printLogEvent("Get", event);
					eventBus.sendEvent(new Event(EventId.ClientOutput, registerCourse(coursesList, event.getMessage())));
					break;
				case DeleteCourses:
					printLogEvent("Get", event);
					String courseId = event.getMessage();
					String result = coursesList.deleteCourse(courseId)
							? "Course " + courseId + " was deleted."
							: "Course not found: " + courseId;
					eventBus.sendEvent(new Event(EventId.ClientOutput, result));
					break;
				case ListCoursesCompleted:
					printLogEvent("Get", event);
					eventBus.sendEvent(new Event(EventId.ClientOutput, enroll(coursesList, event.getMessage())));
					break;
				case StudentDeleted:
					coursesList.removeStudentEnrollments(event.getMessage());
					break;
				case QuitTheSystem:
					eventBus.unRegister(componentId);
					done = true;
					break;
				default:
					break;
				}
			}
		}
	}
	private static String registerCourse(CourseComponent coursesList, String message) {
		if (message == null) {
			return "Enter a course ID, instructor, and name.";
		}
		Course course;
		try {
			course = new Course(message);
		} catch (NoSuchElementException e) {
			return "Enter a course ID, instructor, and name.";
		}
		if (!coursesList.isRegisteredCourse(course.courseId)) {
			coursesList.vCourse.add(course);
			return "This course is successfully added.";
		} else
			return "This course is already registered.";
	}
	private static String enroll(CourseComponent coursesList, String message) {
		// StudentMain sends the student ID, course ID, and zero or more completed course IDs.
		String[] fields = message == null ? new String[0] : message.trim().split("\\s+");
		if (fields.length < 2) {
			return "Enter a student ID and a course ID.";
		}
		String studentId = fields[0];
		String courseId = fields[1];
		Course course = coursesList.findCourse(courseId);
		if (course == null) {
			return "Course not found: " + courseId;
		}
		if (!course.hasCompletedPrerequisites(Arrays.asList(fields).subList(2, fields.length))) {
			return "Prerequisites not completed for course " + courseId + ".";
		}
		if (!course.enrollStudent(studentId)) {
			return "Student " + studentId + " is already enrolled in course " + courseId + ".";
		}
		return "Student " + studentId + " enrolled in course " + courseId + ".";
	}
	private static String makeCourseList(CourseComponent coursesList) {
		String returnString = "";
		for (int j = 0; j < coursesList.vCourse.size(); j++) {
			returnString += coursesList.getCourseList().get(j).getString() + "\n";
		}
		return returnString;
	}
	private static void printLogEvent(String comment, Event event) {
		System.out.println(
				"\n** " + comment + " the event(ID:" + event.getEventId() + ") message: " + event.getMessage());
	}
}
