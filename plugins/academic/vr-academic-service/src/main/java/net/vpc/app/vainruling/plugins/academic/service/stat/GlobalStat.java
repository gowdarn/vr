/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.academic.service.stat;

import net.vpc.app.vainruling.plugins.academic.service.model.config.AcademicSemester;
import net.vpc.app.vainruling.plugins.academic.service.model.config.AcademicTeacherSituation;
import net.vpc.app.vainruling.plugins.academic.service.model.current.AcademicTeacherDegree;

import java.util.*;

/**
 * @author taha.bensalah@gmail.com
 */
public class GlobalStat {

    private int teachersCount;
    private int teachersLeaveCount;
    private int teachersPermanentCount;
    private int teachersTemporaryCount;
    private int teachersContractualCount;
    private int teachersOtherCount;
    private int teachersAssistantTotalNeedCount;
    private int teachersAssistantRequestNeedCount;
    private int courseAssignmentCount;
    private int coursePlanCount;
    /**
     * les charge manquante independament des vacataire et contractuel basee sur
     * la charge dun assistant
     */
    private GlobalAssignmentStat neededAbsolute = new GlobalAssignmentStat();
    /**
     * les charge manquante apres comptabilisation des vacataires et
     * contractuels basee sur la charge d'un assistant
     */
    private GlobalAssignmentStat neededRelative = new GlobalAssignmentStat();
    private GlobalAssignmentStat missing = new GlobalAssignmentStat();
    private List<GlobalAssignmentStat> details = new ArrayList<>();
    private Map<String, GlobalAssignmentStat> detailsMap = new HashMap<>();

    public List<GlobalAssignmentStat> getAssignments() {
        return details;
    }

    public List<GlobalAssignmentStat> getSituationDetails(AcademicSemester semester, AcademicTeacherDegree degree) {
        List<GlobalAssignmentStat> all=new ArrayList<>();
        for (GlobalAssignmentStat detail : details) {
            if(detail.getSituation()!=null && Objects.equals(detail.getDegree(),degree) && Objects.equals(detail.getSemester(),semester)){
                all.add(detail);
            }
        }
        return all;
    }

    public GlobalAssignmentStat getTotalAssignment() {
        return getAssignment(null, null, null);
    }

    public LoadValue getAssignmentSumValue(AcademicSemester semester, List<AcademicTeacherSituation> situations, AcademicTeacherDegree degree) {
        LoadValue v=new LoadValue();
        for (AcademicTeacherSituation situation : situations) {
            v.add(getAssignment(semester,situation,degree).getValue());
        }
        return v;
    }
    public LoadValue getAssignmentSumDue(AcademicSemester semester, List<AcademicTeacherSituation> situations, AcademicTeacherDegree degree) {
        LoadValue v=new LoadValue();
        for (AcademicTeacherSituation situation : situations) {
            v.add(getAssignment(semester,situation,degree).getDue());
        }
        return v;
    }

    public int getAssignmentTeacherCount(AcademicSemester semester, List<AcademicTeacherSituation> situations, AcademicTeacherDegree degree) {
        int count=0;
        for (AcademicTeacherSituation situation : situations) {
            count+=getAssignment(semester,situation,degree).getTeachersCount();
        }
        return count;
    }

    public GlobalAssignmentStat getAssignment(AcademicSemester semester, AcademicTeacherSituation situation, AcademicTeacherDegree degree) {
        String s = (situation == null ? "%" : situation.getId())
                + ";" + (degree == null ? "%" : degree.getId())
                + ";" + (semester == null ? "%" : semester.getId());
        GlobalAssignmentStat r = detailsMap.get(s);
        if (r == null) {
            r = new GlobalAssignmentStat();
            r.setDegree(degree);
            r.setSituation(situation);
            r.setSemester(semester);
            detailsMap.put(s, r);
            details.add(r);
        }
        return r;
    }

    public int getCourseAssignmentCount() {
        return courseAssignmentCount;
    }

    public void setCourseAssignmentCount(int courseAssignmentCount) {
        this.courseAssignmentCount = courseAssignmentCount;
    }

    public int getCoursePlanCount() {
        return coursePlanCount;
    }

    public void setCoursePlanCount(int coursePlanCount) {
        this.coursePlanCount = coursePlanCount;
    }

    public int getTeachersCount() {
        return teachersCount;
    }

    public void setTeachersCount(int teachersCount) {
        this.teachersCount = teachersCount;
    }

    public int getTeachersPermanentCount() {
        return teachersPermanentCount;
    }

    public void setTeachersPermanentCount(int teachersPermanentCount) {
        this.teachersPermanentCount = teachersPermanentCount;
    }

    public int getTeachersTemporaryCount() {
        return teachersTemporaryCount;
    }

    public void setTeachersTemporaryCount(int teachersTemporaryCount) {
        this.teachersTemporaryCount = teachersTemporaryCount;
    }

    public int getTeachersContractualCount() {
        return teachersContractualCount;
    }

    public void setTeachersContractualCount(int teachersContractualCount) {
        this.teachersContractualCount = teachersContractualCount;
    }

    public int getTeachersAssistantTotalNeedCount() {
        return teachersAssistantTotalNeedCount;
    }

    public void setTeachersAssistantTotalNeedCount(int teachersAssistantTotalNeedCount) {
        this.teachersAssistantTotalNeedCount = teachersAssistantTotalNeedCount;
    }

    public int getTeachersAssistantRequestNeedCount() {
        return teachersAssistantRequestNeedCount;
    }

    public void setTeachersAssistantRequestNeedCount(int teachersAssistantRequestNeedCount) {
        this.teachersAssistantRequestNeedCount = teachersAssistantRequestNeedCount;
    }

    /**
     * la charge nécessaire selon le du des enseignant permanents donc en gros
     * combien on a besoin d'assistants pour ne plus recruter des contractuels
     * et vacataires
     *
     * @return
     */
    public GlobalAssignmentStat getNeededAbsolute() {
        return neededAbsolute;
    }

    public void setNeededAbsolute(GlobalAssignmentStat neededAbsolute) {
        this.neededAbsolute = neededAbsolute;
    }

    public GlobalAssignmentStat getNeededRelative() {
        return neededRelative;
    }

    public void setNeededRelative(GlobalAssignmentStat neededRelative) {
        this.neededRelative = neededRelative;
    }

    public int getTeachersOtherCount() {
        return teachersOtherCount;
    }

    public void setTeachersOtherCount(int teachersOtherCount) {
        this.teachersOtherCount = teachersOtherCount;
    }

    public GlobalAssignmentStat getMissing() {
        return missing;
    }


    public int getTeachersLeaveCount() {
        return teachersLeaveCount;
    }

    public void setTeachersLeaveCount(int teachersLeaveCount) {
        this.teachersLeaveCount = teachersLeaveCount;
    }
}
