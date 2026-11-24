/**
 * Copyright(c) 2021 All rights reserved by Jungho Kim in MyungJi University
 */
package Components.ClientInput;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.concurrent.TimeUnit;

import Framework.Event;
import Framework.EventId;
import Framework.EventQueue;
import Framework.RMIEventBus;

public class ClientInputMain {
	public static void main(String[] args) throws IOException, NotBoundException, InterruptedException {
		RMIEventBus eventBus = (RMIEventBus) Naming.lookup("EventBus");
		long componentId = eventBus.register();
		System.out.println("** ClientInputMain(ID:" + componentId + ") is successfully registered.\n");

		try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
			while (true) {
				writeMenu();
				String choice = readLine(input);
				if (choice.equals("0")) {
					break;
				}
				Event event = readRequest(choice, input);
				if (event != null) {
					eventBus.sendEvent(event);
					printLogSend(event.getEventId());
					awaitResult(eventBus, componentId);
				}
			}
		} catch (EOFException e) {
			// End of input closes the session, including when commands are piped in.
		} finally {
			try {
				eventBus.sendEvent(new Event(EventId.QuitTheSystem, "Quit the system!!!"));
				printLogSend(EventId.QuitTheSystem);
			} finally {
				eventBus.unRegister(componentId);
			}
		}
	}

	private static Event readRequest(String choice, BufferedReader input) throws IOException {
		switch (choice) {
		case "1":
			return new Event(EventId.ListStudents);
		case "2":
			return new Event(EventId.ListCourses);
		case "3":
			return new Event(EventId.RegisterStudents, makeStudentInfo(input));
		case "4":
			return new Event(EventId.RegisterCourses, makeCourseInfo(input));
		case "5":
			return new Event(EventId.DeleteStudents, readStudentId(input));
		case "6":
			return new Event(EventId.DeleteCourses, readCourseId(input));
		case "7":
			String courseId = readCourseId(input);
			String studentId = readStudentId(input);
			return new Event(EventId.EnrollCourse, studentId + " " + courseId);
		default:
			System.out.println("Choose a number from the menu.");
			return null;
		}
	}

	private static String makeStudentInfo(BufferedReader input) throws IOException {
		String studentId = readStudentId(input);
		String familyName = readToken(input, "Enter family name (Ex. Hong)>> ");
		String firstName = readToken(input, "Enter first name (Ex. Gildong)>> ");
		String department = readToken(input, "Enter department (Ex. CS)>> ");
		System.out.println("Enter completed course IDs separated by spaces, or press return>> ");
		return String.join(" ", studentId, familyName, firstName, department, readLine(input));
	}

	private static String makeCourseInfo(BufferedReader input) throws IOException {
		String courseId = readCourseId(input);
		String instructor = readToken(input, "Enter the family name of the instructor (Ex. Hong)>> ");
		String name = readToken(input, "Enter the course name with underscores (Ex. C++_Programming)>> ");
		System.out.println("Enter prerequisite course IDs separated by spaces, or press return>> ");
		return String.join(" ", courseId, instructor, name, readLine(input));
	}

	private static String readStudentId(BufferedReader input) throws IOException {
		return readToken(input, "Enter student ID (Ex. 20131234)>> ");
	}

	private static String readCourseId(BufferedReader input) throws IOException {
		return readToken(input, "Enter course ID (Ex. 12345)>> ");
	}

	private static String readToken(BufferedReader input, String prompt) throws IOException {
		while (true) {
			System.out.println(prompt);
			String value = readLine(input);
			if (value.matches("\\S+")) {
				return value;
			}
			System.out.println("Enter one value without spaces.");
		}
	}

	private static String readLine(BufferedReader input) throws IOException {
		String line = input.readLine();
		if (line == null) {
			throw new EOFException();
		}
		return line.trim();
	}

	private static void awaitResult(RMIEventBus eventBus, long componentId)
			throws IOException, InterruptedException {
		// One menu request is in flight at a time, so quit follows its result.
		long start = System.nanoTime();
		while (System.nanoTime() - start < TimeUnit.SECONDS.toNanos(10)) {
			EventQueue queue = eventBus.getEventQueue(componentId);
			while (queue.getSize() > 0) {
				if (queue.getEvent().getEventId() == EventId.ClientOutput) {
					return;
				}
			}
			Thread.sleep(50);
		}
		throw new IOException("No result received. Check that StudentMain and CourseMain are running.");
	}

	private static void writeMenu() {
		System.out.println("1. List Students");
		System.out.println("2. List Courses");
		System.out.println("3. Register a new Student");
		System.out.println("4. Register a new Course");
		System.out.println("5. Delete a Student");
		System.out.println("6. Delete a Course");
		System.out.println("7. Enroll in a Course");
		System.out.println("0. Quit the system");
		System.out.print("\n Choose No.: ");
	}

	private static void printLogSend(EventId eventId) {
		System.out.println("\n** Sending an event(ID:" + eventId + ")\n");
	}
}
