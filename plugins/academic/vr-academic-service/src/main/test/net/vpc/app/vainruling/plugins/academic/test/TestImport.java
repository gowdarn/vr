//package net.vpc.app.vainruling.plugins.academic.internship.test;
//
//import net.vpc.app.vainruling.core.service.TraceService;
//import net.vpc.app.vainruling.core.service.VrApp;
//import net.vpc.app.vainruling.plugins.academic.internship.service.AcademicInternshipPlugin;
//import net.vpc.app.vainruling.plugins.academic.service.AcademicPlugin;
//import net.vpc.common.util.Chronometer;
//
///**
// * this is not a unit test
// */
//public class Ex2 {
//    public static void main(String[] args) {
//        Chronometer ch = new Chronometer();
//        VrApp.runStandalone("taha.bensalah", "myâssword");
//        TraceService trace = TraceService.get();
//        AcademicPlugin aca = VrApp.getBean(AcademicPlugin.class);
//        AcademicInternshipPlugin aci = VrApp.getBean(AcademicInternshipPlugin.class);
//        aci.findActualInternshipsByStudent(0);
//        aci.findActualInternshipsBySupervisor(0);
//        aci.findActualInternshipsByTeacher(1, 1);
//        aci.findInternships(1, -1, 1, 1, 1, true);
//        aci.findInternshipsByDepartment(1, true);
//        aci.findInternshipsByDepartmentExt(1, true);
//        aci.findInternshipsByTeacherExt(1, 1, 1, 1, true);
//        aci.findInternshipTeacherInternshipsCount(1, 1, 1);
//        aci.findInternshipTeachersInternshipsCounts(1, 1);
//        aci.generateInternships(3, "IA3.1");
////        trace.archiveLogs(0);
//        System.out.println(ch.stop());
////        List<AcademicTeacherCV> list = UPA.getPersistenceUnit().findAll(AcademicTeacherCV.class);
////
////
////        for (AcademicTeacherCV r : list) {
////            System.out.println(r);
////        }
//    }
//}
