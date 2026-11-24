/**
 * Copyright(c) 2021 All rights reserved by Jungho Kim in Myungji University
 */
package Components.Course;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class Course {
    protected String courseId;
	protected String instructor;
    protected String name;
    protected ArrayList<String> prerequisiteCoursesList;
    private final Set<String> enrolledStudentIds = new HashSet<>();
    
    public Course(String inputString) {
        StringTokenizer stringTokenizer = new StringTokenizer(inputString);
        this.courseId = stringTokenizer.nextToken();
        this.instructor = stringTokenizer.nextToken();
        this.name = stringTokenizer.nextToken();
        this.prerequisiteCoursesList = new ArrayList<String>();
        while(stringTokenizer.hasMoreTokens()) {
            this.prerequisiteCoursesList.add(stringTokenizer.nextToken());
        }
    }
    public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
    public boolean match(String courseId) {
        return this.courseId.equals(courseId);
    }
    public String getName() {
        return this.name;
    }
    public boolean hasCompletedPrerequisites(Collection<String> completedCourses) {
        return completedCourses.containsAll(prerequisiteCoursesList);
    }
    public boolean enrollStudent(String studentId) {
        return enrolledStudentIds.add(studentId);
    }
    public void removeEnrollment(String studentId) {
        enrolledStudentIds.remove(studentId);
    }
    public String getString() {
        String stringReturn = this.courseId + " " + this.instructor + " " + this.name;
        for(int i = 0; i < this.prerequisiteCoursesList.size(); i++) {
            stringReturn += " " + this.prerequisiteCoursesList.get(i).toString();
        }
        return stringReturn;
    }
}
