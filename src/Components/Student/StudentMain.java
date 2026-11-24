/**
 * Copyright(c) 2021 All rights reserved by Jungho Kim in MyungJi University 
 */

package Components.Student;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.NoSuchElementException;

import Framework.Event;
import Framework.EventId;
import Framework.EventQueue;
import Framework.RMIEventBus;

public class StudentMain {
	public static void main(String args[]) throws FileNotFoundException, IOException, NotBoundException {
		StudentComponent studentsList = new StudentComponent("Students.txt");
		RMIEventBus eventBus = (RMIEventBus) Naming.lookup("EventBus");
		long componentId = eventBus.register();
		System.out.println("** StudentMain(ID:" + componentId + ") is successfully registered. \n");

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
				case ListStudents:
					printLogEvent("Get", event);
					eventBus.sendEvent(new Event(EventId.ClientOutput, makeStudentList(studentsList)));
					break;
				case RegisterStudents:
					printLogEvent("Get", event);
					eventBus.sendEvent(new Event(EventId.ClientOutput, registerStudent(studentsList, event.getMessage())));
					break;
				case DeleteStudents:
					printLogEvent("Get", event);
					String studentId = event.getMessage();
					if (studentsList.deleteStudent(studentId)) {
						eventBus.sendEvent(new Event(EventId.StudentDeleted, studentId));
						eventBus.sendEvent(new Event(EventId.ClientOutput, "Student " + studentId + " was deleted."));
					} else {
						eventBus.sendEvent(new Event(EventId.ClientOutput, "Student not found: " + studentId));
					}
					break;
				case EnrollCourse:
					printLogEvent("Get", event);
					eventBus.sendEvent(checkStudent(studentsList, event.getMessage()));
					break;
				case QuitTheSystem:
					printLogEvent("Get", event);
					eventBus.unRegister(componentId);
					done = true;
					break;
				default:
					break;
				}
			}
		}
	}
	private static String registerStudent(StudentComponent studentsList, String message) {
		if (message == null) {
			return "Enter a student ID, family name, first name, and department.";
		}
		Student student;
		try {
			student = new Student(message);
		} catch (NoSuchElementException e) {
			return "Enter a student ID, family name, first name, and department.";
		}
		if (!studentsList.isRegisteredStudent(student.studentId)) {
			studentsList.vStudent.add(student);
			return "This student is successfully added.";
		} else
			return "This student is already registered.";
	}
	private static Event checkStudent(StudentComponent studentsList, String message) {
		String[] ids = message == null ? new String[0] : message.trim().split("\\s+");
		if (ids.length != 2) {
			return new Event(EventId.ClientOutput, "Enter a student ID and a course ID.");
		}
		Student student = studentsList.findStudent(ids[0]);
		if (student == null) {
			return new Event(EventId.ClientOutput, "Student not found: " + ids[0]);
		}
		String completedCourses = String.join(" ", student.getCompletedCourses());
		return new Event(EventId.ListCoursesCompleted, ids[0] + " " + ids[1] + " " + completedCourses);
	}
	private static String makeStudentList(StudentComponent studentsList) {
		String returnString = "";
		for (int j = 0; j < studentsList.vStudent.size(); j++) {
			returnString += studentsList.getStudentList().get(j).getString() + "\n";
		}
		return returnString;
	}
	private static void printLogEvent(String comment, Event event) {
		System.out.println(
				"\n** " + comment + " the event(ID:" + event.getEventId() + ") message: " + event.getMessage());
	}
}
