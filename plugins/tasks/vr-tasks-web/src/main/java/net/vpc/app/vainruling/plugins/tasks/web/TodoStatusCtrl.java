/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.tasks.web;

import java.util.List;
import javax.faces.bean.ManagedBean;
import net.vpc.app.vainruling.plugins.tasks.service.TaskPlugin;
import net.vpc.app.vainruling.plugins.tasks.service.model.TodoList;
import net.vpc.app.vainruling.plugins.tasks.service.model.TodoStatus;
import net.vpc.app.vainruling.api.web.UCtrl;
import net.vpc.app.vainruling.api.web.UPathItem;
import net.vpc.app.vainruling.api.web.ctrl.AbstractNameCtrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author vpc
 */
@UCtrl(
        breadcrumb = {
            @UPathItem(title = "Parametrage", css = "fa-dashboard", ctrl = ""),
        }
        ,css = "fa-table"
        ,title = "Etats Todo"
        ,url = "modules/todo/config-todo-status"
)
@ManagedBean
@Scope(value = "session")
public class TodoStatusCtrl extends AbstractNameCtrl<TodoStatus> {

    @Autowired
    private TaskPlugin todoService;

    public TodoStatusCtrl() {
        super(new PModel());
    }

    @Override
    public PModel getModel() {
        return (PModel) super.getModel();
    }

    @Override
    public TodoStatus delegated_newInstance() {
        return new TodoStatus();
    }

    @Override
    public void delegated_newCurrent() {
        TodoStatus c = getModel().getCurrent();
        c.setName("Nouveau");
    }

    @Override
    public void delegated_deleteCurrent() {
        TodoStatus c = getModel().getCurrent();
        todoService.removeTodo(c.getId());
    }

    @Override
    public void delegated_saveCurrent() {
        TodoStatus c = getModel().getCurrent();
        if (c.getList() == null) {
            TodoList list = todoService.findTodoList(getModel().getListId());
            c.setList(list);
        }
        todoService.saveTodoStatus(c);
    }

    @Override
    public List<TodoStatus> delegated_findAll() {
        return todoService.findTodoStatuses(getModel().getListId());
    }

    public static class PModel extends Model<TodoStatus> {

        private int listId;

        public int getListId() {
            return listId;
        }

        public void setListId(int listId) {
            this.listId = listId;
        }

    }
}
