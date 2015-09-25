/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.tasks.web;

import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;
import net.vpc.app.vainruling.api.web.UCtrl;
import net.vpc.app.vainruling.plugins.tasks.service.model.TodoPriority;
import net.vpc.app.vainruling.plugins.tasks.service.model.TodoStatusType;

/**
 *
 * @author vpc
 */
@UCtrl
@ManagedBean
public class DropDownCtrl {

    public SelectItem[] getTodoPriorities() {
        SelectItem[] items = new SelectItem[TodoPriority.values().length];
        int i = 0;
        items[i++] = new SelectItem(TodoPriority.VERY_LOW, "tres basse");
        items[i++] = new SelectItem(TodoPriority.LOW, "basse");
        items[i++] = new SelectItem(TodoPriority.DEFAULT, "normale");
        items[i++] = new SelectItem(TodoPriority.HIGH, "haute");
        items[i++] = new SelectItem(TodoPriority.VERY_HIGH, "tres haute");
        items[i++] = new SelectItem(TodoPriority.EXTREMELY_URGENT, "immediate");
        return items;
    }
    
    public SelectItem[] getTodoStatusTypes() {
        SelectItem[] items = new SelectItem[TodoStatusType.values().length];
        int i = 0;
        items[i++] = new SelectItem(TodoStatusType.UNASSIGNED, "à assigner");
        items[i++] = new SelectItem(TodoStatusType.ASSIGNED, "en cours");
        items[i++] = new SelectItem(TodoStatusType.TO_VERIFY, "à vérifier");
        items[i++] = new SelectItem(TodoStatusType.DONE, "terminé");
        return items;
    }
}
