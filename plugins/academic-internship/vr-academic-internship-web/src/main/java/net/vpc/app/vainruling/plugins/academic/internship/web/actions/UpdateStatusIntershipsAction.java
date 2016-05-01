/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.academic.internship.web.actions;

import java.util.List;
import net.vpc.app.vainruling.api.EntityAction;
import net.vpc.app.vainruling.api.VrApp;
import net.vpc.app.vainruling.api.web.ctrl.EditCtrlMode;
import net.vpc.app.vainruling.api.web.obj.ActionDialog;
import net.vpc.app.vainruling.plugins.academic.internship.service.model.current.AcademicInternship;

/**
 *
 * @author vpc
 */
@EntityAction(entityType = AcademicInternship.class,
        actionLabel = "phases", actionStyle = "fa-envelope-o",
        dialog = true
)
public class UpdateStatusIntershipsAction implements ActionDialog {

    @Override
    public void openDialog(String actionId, List<String> itemIds) {
        VrApp.getBean(UpdateStatusIntershipsActionCtrl.class).openDialog();
    }

    @Override
    public boolean isEnabled(Class entityType, EditCtrlMode mode, Object value) {
        return value == null;
    }

    @Override
    public void invoke(Class entityType, Object obj, Object[] args) {
        //do nothing!
    }
}