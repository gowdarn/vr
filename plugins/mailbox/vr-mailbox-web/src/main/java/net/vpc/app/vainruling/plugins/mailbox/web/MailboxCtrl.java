/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.mailbox.web;

import net.vpc.app.vainruling.core.service.CorePlugin;
import net.vpc.app.vainruling.core.service.VrApp;
import net.vpc.app.vainruling.core.service.model.AppUser;
import net.vpc.app.vainruling.core.service.obj.AppFile;
import net.vpc.app.vainruling.core.service.security.UserSession;
import net.vpc.app.vainruling.core.service.util.VrUtils;
import net.vpc.app.vainruling.core.web.OnPageLoad;
import net.vpc.app.vainruling.core.web.UCtrl;
import net.vpc.app.vainruling.core.web.UCtrlData;
import net.vpc.app.vainruling.core.web.UCtrlProvider;
import net.vpc.app.vainruling.core.web.ctrl.EditCtrlMode;
import net.vpc.app.vainruling.core.web.menu.*;
import net.vpc.app.vainruling.core.web.obj.DialogResult;
import net.vpc.app.vainruling.core.web.obj.PropertyView;
import net.vpc.app.vainruling.core.web.obj.PropertyViewManager;
import net.vpc.app.vainruling.core.web.obj.dialog.ProfileExprDialogCtrl;
import net.vpc.app.vainruling.plugins.inbox.service.MailboxPlugin;
import net.vpc.app.vainruling.plugins.inbox.service.model.*;
import net.vpc.common.jsf.FacesUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.upa.PersistenceUnit;
import net.vpc.upa.UPA;
import net.vpc.upa.VoidAction;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author taha.bensalah@gmail.com
 */
@UCtrl
public class MailboxCtrl implements UCtrlProvider, VRMenuDefFactory {

    private static final Logger log = Logger.getLogger(MailboxCtrl.class.getName());
    private Model model = new Model();
    @Autowired
    private MailboxPlugin mailboxPlugin;
    @Autowired
    private CorePlugin corePlugin;

    @Autowired
    private PropertyViewManager propertyViewManager;

    public void onNew() {
        MailboxSent item = new MailboxSent();
        item.setRichText(true);
        resetNewMessage(item,EditCtrlMode.NEW);
    }

    private void resetNewMessage(MailboxSent item,EditCtrlMode mode){
        getModel().setNewItem(item);
        getModel().setNewItemAttachments(new ArrayList<>());
        getModel().setMode(mode);
        getModel().setToCount(0);
        getModel().setCcCount(0);
        getModel().setBccCount(0);
        resetCurrent();
    }
    private void resetCurrent(){
        getModel().setCurrent(null);
        getModel().setThreadList(Collections.emptyList());
    }

    @Override
    public UCtrlData getUCtrl(String cmd) {
        Config config = VrUtils.parseJSONObject(cmd, Config.class);
        if (config == null) {
            config = new Config();
        }
        if (config.folder == null) {
            config.folder = MailboxFolder.CURRENT;
        }
        if(config.sent) {
            switch (config.folder) {
                case CURRENT: {
                    return new UCtrlData(getPreferredTitle(config.folder, config.sent), "Messages Envoyés", "modules/mailbox/mailbox", "fa-table", "Custom.Site.Mailbox",
                            new BreadcrumbItem("Social", "Messages", "fa-dashboard", "", "")
                    );
                }
                case DELETED: {
                    return new UCtrlData(getPreferredTitle(config.folder, config.sent), "Messages Envoyés Effacés", "modules/mailbox/mailbox", "fa-table", "Custom.Site.Mailbox",
                            new BreadcrumbItem("Social", "Messages", "fa-dashboard", "", "")
                    );
                }
                case ARCHIVED: {
                    return new UCtrlData(getPreferredTitle(config.folder, config.sent), "Messages Envoyés Archivés", "modules/mailbox/mailbox", "fa-table", "Custom.Site.Mailbox",
                            new BreadcrumbItem("Social", "Messages", "fa-dashboard", "", "")
                    );
                }
            }
        }else{
            switch (config.folder) {
                case CURRENT: {
                    return new UCtrlData(getPreferredTitle(config.folder, config.sent), "Messages Reçus", "modules/mailbox/mailbox", "fa-table", "Custom.Site.Mailbox",
                            new BreadcrumbItem("Social", "Messages", "fa-dashboard", "", "")
                    );
                }
                case DELETED: {
                    return new UCtrlData(getPreferredTitle(config.folder, config.sent), "Messages Reçus Effacés", "modules/mailbox/mailbox", "fa-table", "Custom.Site.Mailbox",
                            new BreadcrumbItem("Social", "Messages", "fa-dashboard", "", "")
                    );
                }
                case ARCHIVED: {
                    return new UCtrlData(getPreferredTitle(config.folder, config.sent), "Messages Reçus Archivés", "modules/mailbox/mailbox", "fa-table", "Custom.Site.Mailbox",
                            new BreadcrumbItem("Social", "Messages", "fa-dashboard", "", "")
                    );
                }
            }
        }
        return null;
    }

    public String getPreferredTitle() {
        return getPreferredTitle(getModel().getFolder(), getModel().isSent());
    }

    public String getPreferredTitle(MailboxFolder f, boolean sent) {
        if (sent) {
            switch (f) {
                case CURRENT: {
                    return "Boîte Envoi";
                }
                case DELETED: {
                    return "Corbeille";
                }
                case ARCHIVED: {
                    return "Archive";
                }
            }
        } else {
            switch (f) {
                case CURRENT: {
                    return "Boîte réception";
                }
                case DELETED: {
                    return "Corbeille";
                }
                case ARCHIVED: {
                    return "Archive";
                }
            }
        }
        return "Mes Messages";
    }

    @Override
    public List<VRMenuDef> createVRMenuDefList() {
        List<Row> list = new ArrayList<>();
        AppUser currentUser = UserSession.getCurrentUser();
        int userId = currentUser == null ? -1 : currentUser.getId();
        int count = userId < 0 ? 0 : mailboxPlugin.findLocalReceivedMessages(userId, -1, true, MailboxFolder.CURRENT).size();
        return Arrays.asList(
                new VRMenuDef("Mes Messages", "/Social", "mailbox", "{folder:'CURRENT',sent:false}", "Custom.Site.Mailbox", "", 100,
                        new VRMenuLabel[]{
                                count <= 0 ? null : new VRMenuLabel(String.valueOf(count), "success")
                        }
                )//,
                //                new VRMenuDef("Messages Envoyés", "/Social", "mailbox", "{folder:'CURRENT',sent:true}", "Custom.Site.Outbox","")
                //                , new VRMenuDef("Mes Messages Archivés", "/Social", "mailbox", "{folder:'ARCHIVED'}", "Custom.Site.Mailbox.Archived")
        );
    }

    @OnPageLoad
    public void onPageLoad(Config config) {
        if (config == null) {
            config = new Config();
        }
        if (config.folder == null) {
            config.folder = MailboxFolder.CURRENT;
        }
        getModel().setFolder(config.folder);
        getModel().setSent(config.sent);
        onCancelCurrent();
        getModel().setMailboxMessageFormat(propertyViewManager.createPropertyView(MailboxMessageFormat.class));
        onRefresh();
    }

    public void onRefresh() {
        if (FacesContext.getCurrentInstance() != null) {
            FacesUtils.clearMessages();
        }
        List<Row> list = new ArrayList<>();
        int userId = UserSession.getCurrentUser().getId();
        getModel().setTitle(getPreferredTitle());
        VrApp.getBean(VrMenuManager.class).getModel().getTitleCrumb().setTitle(getModel().getTitle());
        if (getModel().isSent()) {
            List<MailboxSent> mailboxSents = mailboxPlugin.findLocalSentMessages(userId, -1, -1, false, getModel().getFolder());
            for (MailboxSent inbox : mailboxSents) {
                Row r = new Row();
                r.setValue(new Message(inbox, loadMailboxFiles(inbox.getId())));
                r.setHasAttachment(r.getValue().hasAttachment());
                list.add(r);
            }
        } else {
            for (MailboxReceived inbox : mailboxPlugin.findLocalReceivedMessages(userId, -1, false, getModel().getFolder())) {
                Row r = new Row();
                r.setValue(new Message(inbox, loadMailboxFiles(inbox.getId())));
                r.setHasAttachment(r.getValue().hasAttachment());
                list.add(r);
            }
        }
        model.setInbox(list);
        getModel().setCountInbox(mailboxPlugin.findLocalReceivedMessages(userId, -1, true, MailboxFolder.CURRENT).size());
        getModel().setCountOutbox(mailboxPlugin.findLocalSentMessages(userId, -1, -1,true, MailboxFolder.CURRENT).size());
    }

    private List<AppFile> loadMailboxFiles(int id) {
        if (getModel().isSent()){
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }


    public void updateUserCounts() {
        getModel().setToCount(countUsers("to"));
        getModel().setCcCount(countUsers("cc"));
        getModel().setBccCount(countUsers("bcc"));
    }

    public List<String> completeProfileExpr(String query) {
        return corePlugin.autoCompleteProfileExpression(query);
    }

    public List<String> completeCategoryExpr(String query) {
        return mailboxPlugin.autoCompleteCategoryExpression(query);
    }

    public Model getModel() {
        return model;
    }

    public void onSelectInboxFolder() {
        FacesUtils.clearMessages();
        getModel().setSent(false);
        getModel().setFolder(MailboxFolder.CURRENT);
        getModel().setMode(EditCtrlMode.LIST);
        resetCurrent();
        onRefresh();
    }

    public void onSelectDeletedInboxFolder() {
        FacesUtils.clearMessages();
        getModel().setSent(false);
        getModel().setFolder(MailboxFolder.DELETED);
        getModel().setMode(EditCtrlMode.LIST);
        resetCurrent();
        onRefresh();
    }

    public void onSelectArchivedInboxFolder() {
        FacesUtils.clearMessages();
        getModel().setSent(false);
        getModel().setFolder(MailboxFolder.ARCHIVED);
        getModel().setMode(EditCtrlMode.LIST);
        resetCurrent();
        onRefresh();
    }

    public void onSelectOutboxFolder() {
        FacesUtils.clearMessages();
        getModel().setSent(true);
        getModel().setFolder(MailboxFolder.CURRENT);
        getModel().setMode(EditCtrlMode.LIST);
        resetCurrent();
        onRefresh();
    }

    public void onSelectDeletedOutboxFolder() {
        FacesUtils.clearMessages();
        getModel().setSent(true);
        getModel().setFolder(MailboxFolder.DELETED);
        getModel().setMode(EditCtrlMode.LIST);
        resetCurrent();
        onRefresh();
    }

    public void onSelectArchivedOutboxFolder() {
        FacesUtils.clearMessages();
        getModel().setSent(true);
        getModel().setFolder(MailboxFolder.ARCHIVED);
        getModel().setMode(EditCtrlMode.LIST);
        resetCurrent();
        onRefresh();
    }

    private void loadThreadList(Message r,boolean ignoreThis) {
        ArrayList<Message> messages = new ArrayList<>();
        if(r.getThreadId()>0){
            for (MailboxReceived mailboxReceived : mailboxPlugin.findLocalReceivedMessagesThread(corePlugin.getCurrentUserId(), -1, r.getThreadId(),false,getModel().getFolder())) {
                if(!ignoreThis || mailboxReceived.getId()!=r.getId()) {
                    messages.add(new Message(mailboxReceived, loadMailboxFiles(mailboxReceived.getId())));
                }
            }
        }
        getModel().setThreadList(messages);
    }

    public void onSelect(Message r) {
        if (r != null) {
            getModel().setCurrent(r);
            loadThreadList(r,true);
            getModel().setMode(EditCtrlMode.UPDATE);
            if(r.isInbox()) {
                MailboxReceived m = (MailboxReceived) r.getMsg();
                mailboxPlugin.markRead(m.getId(), true);
                m.setRead(true);//no need for refresh
            }
        }
    }

    public void onCancelCurrent() {
        FacesUtils.clearMessages();
        MailboxSent item = new MailboxSent();
        item.setRichText(true);
        getModel().setNewItem(item);
        getModel().setNewItemAttachments(new ArrayList<>());
        getModel().setMode(EditCtrlMode.LIST);
    }

    public void onRemoveSelected() {
        FacesUtils.clearMessages();
        String currentUserLogin = corePlugin.getCurrentUserLogin();
        PersistenceUnit pu = UPA.getPersistenceUnit();
        pu.invokePrivileged(new VoidAction() {
            @Override
            public void run() {
                switch (getModel().getMode()) {
                    case LIST: {
                        if (getModel().getFolder() == MailboxFolder.DELETED) {
                            for (Row inbox : getModel().getInbox()) {
                                if (inbox.isSelected()) {
                                    pu.remove(inbox.value.getMsg());
                                }
                            }
                            onRefresh();
                            return;
                        }
                        for (Row inbox : getModel().getInbox()) {
                            if (inbox.isSelected()) {
                                inbox.getValue().setDeleted(true);
                                inbox.getValue().setDeletedBy(currentUserLogin);
                                pu.merge(inbox.getValue().getMsg());
                            }
                        }
                        onRefresh();
                        break;
                    }
                    case UPDATE: {
                        if (getModel().getCurrent() != null && getModel().getCurrent().getMsg() != null) {
                            if (getModel().getFolder() == MailboxFolder.DELETED) {
                                pu.remove(getModel().getCurrent().getMsg());
                                onRefresh();
                                return;
                            }
                            Message msg = (Message) getModel().getCurrent().getMsg();
                            msg.setDeleted(true);
                            msg.setDeletedBy(currentUserLogin);
                            pu.merge(msg.getMsg());
                        }
                        getModel().setMode(EditCtrlMode.LIST);
                        onRefresh();
                        break;
                    }
                }

            }
        });
    }

    public void onArchiveSelected() {
        FacesUtils.clearMessages();
//        String currentUserLogin = corePlugin.getCurrentUserLogin();
        PersistenceUnit pu = UPA.getPersistenceUnit();
        pu.invokePrivileged(new VoidAction() {
            @Override
            public void run() {
                for (Row inbox : getModel().getInbox()) {
                    if (inbox.isSelected()) {
                        inbox.getValue().setArchived(true);
                        pu.merge(inbox.getValue().getMsg());
                    }
                }
            }
        });
        onRefresh();
    }

//    public void onEraseSelected() {
//        FacesUtils.clearMessages();
//        for (Row inbox : getModel().getInbox()) {
//            if (inbox.isSelected()) {
//                UPA.getPersistenceUnit().remove(inbox.value.getMsg());
//            }
//        }
//        onRefresh();
//    }

    public void onSend() {
        FacesUtils.clearMessages();
        getModel().getNewItem().setSender(UserSession.getCurrentUser());
        FacesUtils.clearMessages();
        try {
            MailboxMessageFormat mailboxMessageFormat = (MailboxMessageFormat) getModel().getMailboxMessageFormat().getValue();
            mailboxPlugin.sendLocalMail(getModel().getNewItem(), mailboxMessageFormat == null ? null : mailboxMessageFormat.getId(), true);
            MailboxSent item = new MailboxSent();
            item.setRichText(true);
            getModel().setNewItem(item);
            getModel().setNewItemAttachments(new ArrayList<>());
            getModel().setMode(EditCtrlMode.LIST);
            onRefresh();
            FacesUtils.addInfoMessage("Envoi réussi");
        } catch (Exception e) {
            FacesUtils.addErrorMessage("Envoi impossible : " + e.getMessage());
        }
    }

    public String evalProfileMinusLogin(String profile, String login) {
        if (StringUtils.isEmpty(login) || StringUtils.isEmpty(login)) {
            return "";
        }
        if (profile.trim().equals(login)) {
            return "";
        }
        return "(" + profile + ")-" + login;
    }

    public void onReply() {
        FacesUtils.clearMessages();
        MailboxSent s = new MailboxSent();
        Message current = getModel().getCurrent();
        if (current != null && current.isInbox()) {
            MailboxReceived r = (MailboxReceived) current.getMsg();
            s.setToProfiles(r.getSender().getLogin());
            s.setCcProfiles(r.getCcProfiles());
            String subject = r.getSubject();
            if(!subject.startsWith("RE:")){
                subject="RE:" + subject;
            }
            s.setSubject(subject);
            s.setRichText(true);
            s.setThreadId(r.getOutboxMessage()==null?0:r.getOutboxMessage().getThreadId());
            resetNewMessage(s,EditCtrlMode.NEW);
            loadThreadList(current,false);
        }
    }

    public void onReplyToAll() {
        FacesUtils.clearMessages();
        MailboxSent s = new MailboxSent();
        Message current = getModel().getCurrent();
        if (current != null && current.isInbox()) {
            MailboxReceived r = (MailboxReceived) current.getMsg();
            String ee = evalProfileMinusLogin(r.getToProfiles(), r.getOwner().getLogin());
            s.setToProfiles("" + r.getSender().getLogin() + (StringUtils.isEmpty(ee) ? "" : ",(" + ee + ")"));
            s.setCcProfiles(r.getCcProfiles());
            String subject = r.getSubject();
            if(!subject.startsWith("RE:")){
                subject="RE:" + subject;
            }
            s.setThreadId(r.getOutboxMessage()==null?0:r.getOutboxMessage().getThreadId());
            s.setRichText(true);
            resetNewMessage(s,EditCtrlMode.NEW);
            loadThreadList(current,false);
        }
    }

    public void onAdvancedSettings() {
        FacesUtils.clearMessages();
        getModel().setAdvancedSettings(!getModel().isAdvancedSettings());
    }

    public boolean isEnabledButton(String buttonId) {
        switch (getModel().getMode()) {
            case NEW: {
                if ("Send".equals(buttonId)) {
                    return true;
                }
                if ("Cancel".equals(buttonId)) {
                    return true;
                }
                if ("BackToInbox".equals(buttonId)) {
                    return true;
                }
                return "Advanced".equals(buttonId);
            }
            case UPDATE: {
                if ("Cancel".equals(buttonId)) {
                    return true;
                }
                if ("Reply".equals(buttonId)) {
                    if(getModel().getCurrent()!=null && getModel().getCurrent().isInbox()) {
                        //check rights?
                        return true;
                    }
                }
                if ("Remove".equals(buttonId)) {
                    //check rights?
                    return true;
                }
                if ("BackToInbox".equals(buttonId)) {
                    return true;
                }
                return "Archive".equals(buttonId);
            }
            case LIST: {
                if ("Refresh".equals(buttonId)) {
                    return true;
                }
                if ("New".equals(buttonId)) {
                    return true;
                }
                if ("Remove".equals(buttonId)) {
                    return true;
                }
                if ("Archive".equals(buttonId)) {
                    return true;
                }
                if ("Reply".equals(buttonId)) {
                    return !getModel().isSent() && getModel().getCurrent() != null;
                }
                if ("BackToInbox".equals(buttonId)) {
                    return false;
                }
                return false;
            }
        }
        return false;
    }

    public boolean isListMode() {
        try {

            return getModel().getMode() == EditCtrlMode.LIST;
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw ex;
        }
    }

    public boolean isNewOrUpdateMode() {
        try {
            return getModel().getMode() == EditCtrlMode.NEW || getModel().getMode() == EditCtrlMode.UPDATE;
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw ex;
        }
    }

    public boolean isNewMode() {
        try {
            return getModel().getMode() == EditCtrlMode.NEW;
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw ex;
        }
    }

    public int countUsers(String action) {
        String e = null;
        if ("to".equals(action)) {
            e = getModel().getNewItem().getToProfiles();
        } else if ("cc".equals(action)) {
            e = getModel().getNewItem().getCcProfiles();
        } else if ("bcc".equals(action)) {
            e = getModel().getNewItem().getBccProfiles();
        }
        return corePlugin.findUsersByProfileFilter(e, null).size();
    }

    public void openProfileExprDialog(String action) {
        ProfileExprDialogCtrl.Config c = new ProfileExprDialogCtrl.Config();
        c.setSourceId(action);
        c.setUserInfo(action);
        c.setTitle(action);
        String e = null;
        if ("to".equals(action)) {
            e = getModel().getNewItem().getToProfiles();
        } else if ("cc".equals(action)) {
            e = getModel().getNewItem().getCcProfiles();
        } else if ("bcc".equals(action)) {
            e = getModel().getNewItem().getBccProfiles();
        }
        c.setExpression(e);
        if (getModel().getNewItem() != null && getModel().getNewItem().isExternalMessage()) {
            c.setEmails(true);
        }
        VrApp.getBean(ProfileExprDialogCtrl.class).openDialog(c);
    }

    public void onProfileExprDialogClosed(SelectEvent event) {
        DialogResult o = (DialogResult) event.getObject();
        if (o != null && o.getUserInfo() != null) {
            String d = o.getUserInfo();
            if ("to".equals(d)) {
                getModel().getNewItem().setToProfiles((String) o.getValue());
            } else if ("cc".equals(d)) {
                getModel().getNewItem().setCcProfiles((String) o.getValue());
            } else if ("bcc".equals(d)) {
                getModel().getNewItem().setBccProfiles((String) o.getValue());
            }
        }
        updateUserCounts();
    }

    public static class Row {

        Message value;
        boolean selected;
        boolean hasAttachment;

        public Message getValue() {
            return value;
        }

        public void setValue(Message value) {
            this.value = value;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isHasAttachment() {
            return hasAttachment;
        }

        public void setHasAttachment(boolean hasAttachment) {
            this.hasAttachment = hasAttachment;
        }
    }

    public void onHandleProjectFileUpload(FileUploadEvent event) {
        try {
//            ApblProject p = getModel().getSelectedProject();
//            ApblSession s = p.getSession();
//            VFile file = VrWebHelper.handleFileUpload(event, "/Documents/Services/Ext/pbl/" + s.getId() + "-" + s.getName() + "/Projects/" + p.getId() + "-" + p.getName() + "/*", false, true);
//            if (file != null) {
//                getModel().setSelectedPathBeforeUpload(p.getSpecFilePath());
//                getModel().setSelectedPathUploaded(file.getPath());
//                p.setSpecFilePath(file.getPath());
//            }
//            RequestContext.getCurrentInstance().update("myform:pathComp");

        } catch (Exception ex) {
            FacesUtils.addErrorMessage("Upload échoué.", ex.getMessage());
        }
    }

    public static class Config {

        public MailboxFolder folder;
        public boolean sent = false;
    }

    public void switchReadFlag(Row row) {
        if (row.getValue().isInbox()) {
            Message v = row.getValue();
            v.setRead(!v.isRead());
            mailboxPlugin.markRead(((MailboxReceived) v.getMsg()).getId(), v.isRead());
        }
    }

    public static class Model {

        private MailboxFolder folder = MailboxFolder.CURRENT;
        private boolean sent = false;
        private String title = "Inbox";
        private int countInbox = 0;
        private int countOutbox = 0;
        private int toCount = 0;
        private int ccCount = 0;
        private int bccCount = 0;
        private Message current;
        private List<Message> threadList=new ArrayList<>();
        private PropertyView mailboxMessageFormat;
        private MailboxSent newItem = new MailboxSent();
        private List<MailboxSentAttachment> newItemAttachments = new ArrayList<>();
        private List<Row> inbox = new ArrayList<>();
        private EditCtrlMode mode = EditCtrlMode.LIST;
        private boolean advancedSettings;

        public int getToCount() {
            return toCount;
        }

        public void setToCount(int toCount) {
            this.toCount = toCount;
        }

        public int getCcCount() {
            return ccCount;
        }

        public void setCcCount(int ccCount) {
            this.ccCount = ccCount;
        }

        public int getBccCount() {
            return bccCount;
        }

        public void setBccCount(int bccCount) {
            this.bccCount = bccCount;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getCountInbox() {
            return countInbox;
        }

        public void setCountInbox(int countInbox) {
            this.countInbox = countInbox;
        }

        public int getCountOutbox() {
            return countOutbox;
        }

        public void setCountOutbox(int countOutbox) {
            this.countOutbox = countOutbox;
        }

        public List<Row> getInbox() {
            return inbox;
        }

        public void setInbox(List<Row> inbox) {
            this.inbox = inbox;
        }

        public Message getCurrent() {
            return current;
        }

        public void setCurrent(Message current) {
            this.current = current;
        }

        public EditCtrlMode getMode() {
            return mode;
        }

        public void setMode(EditCtrlMode mode) {
            this.mode = mode;
        }

        public MailboxSent getNewItem() {
            return newItem;
        }

        public void setNewItem(MailboxSent newItem) {
            this.newItem = newItem;
        }

        public List<MailboxSentAttachment> getNewItemAttachments() {
            return newItemAttachments;
        }

        public void setNewItemAttachments(List<MailboxSentAttachment> newItemAttachments) {
            this.newItemAttachments = newItemAttachments;
        }

        public MailboxFolder getFolder() {
            return folder;
        }

        public void setFolder(MailboxFolder folder) {
            this.folder = folder;
        }

        public boolean isAdvancedSettings() {
            return advancedSettings;
        }

        public void setAdvancedSettings(boolean advancedSettings) {
            this.advancedSettings = advancedSettings;
        }

        public boolean isSent() {
            return sent;
        }

        public void setSent(boolean sent) {
            this.sent = sent;
        }

        public PropertyView getMailboxMessageFormat() {
            return mailboxMessageFormat;
        }

        public void setMailboxMessageFormat(PropertyView mailboxMessageFormat) {
            this.mailboxMessageFormat = mailboxMessageFormat;
        }

        public List<Message> getThreadList() {
            return threadList;
        }

        public void setThreadList(List<Message> threadList) {
            this.threadList = threadList;
        }
    }
}
