/**
 * Copyright(c) 2021 All rights reserved by Jungho Kim in Myungji University
 */
package Components.Course;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CourseComponent {
    protected ArrayList<Course> vCourse;

    public CourseComponent(String sCourseFileName) throws FileNotFoundException, IOException { 	
        BufferedReader bufferedReader  = new BufferedReader(new FileReader(sCourseFileName));       
        this.vCourse  = new ArrayList<Course>();
        while (bufferedReader.ready()) {
            String courseInfo = bufferedReader.readLine();
            if(!courseInfo.equals("")) this.vCourse.add(new Course(courseInfo));
        }    
        bufferedReader.close();
    }
    public ArrayList<Course> getCourseList() {
        return this.vCourse;
    }
    public boolean isRegisteredCourse(String courseId) {
        return findCourse(courseId) != null;
    }
    public Course findCourse(String courseId) {
        for (Course course : vCourse) {
            if (course.match(courseId)) {
                return course;
            }
        }
        return null;
    }
    public boolean deleteCourse(String courseId) {
        return vCourse.removeIf(course -> course.match(courseId));
    }
    public void removeStudentEnrollments(String studentId) {
        for (Course course : vCourse) {
            course.removeEnrollment(studentId);
        }
    }
}
