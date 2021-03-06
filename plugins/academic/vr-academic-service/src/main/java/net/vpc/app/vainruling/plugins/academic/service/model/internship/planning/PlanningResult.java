package net.vpc.app.vainruling.plugins.academic.service.model.internship.planning;

/**
 * Created by vpc on 5/26/16.
 */
public class PlanningResult {
    private PlanningActivityTable resut;
    private FitnessValue fitness;

    public PlanningResult(PlanningActivityTable resut, FitnessValue fitness) {
        this.resut = resut;
        this.fitness = fitness;
    }

    public PlanningActivityTable getResut() {
        return resut;
    }

    public FitnessValue getFitness() {
        return fitness;
    }
}
