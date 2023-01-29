package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class WhatsappRepository {

    //Users
    HashMap<String,User> userHashMap;
    HashMap<User, List<Integer>> userMessageDb;
    //HashMap<String,String> userGroupDb;

    //Group
    HashMap<String,Group> groupHashMap;
    HashMap<Group,User> groupAdminDb;
    HashMap<Group,List<User>> groupUsersDb;
    HashMap<Group ,List<Integer>> groupMessageDb;

    //Message
    HashMap<Integer,Message> messageHashMap;
    int groupCount ;

    public WhatsappRepository(){

        userHashMap = new HashMap<>();
        userMessageDb = new HashMap<>();

        groupHashMap = new HashMap<>();
        groupUsersDb = new HashMap<>();
        groupAdminDb = new HashMap<>();
        groupMessageDb = new HashMap<>();

        messageHashMap = new HashMap<>();
        groupCount=0;
    }

    public String createUser(String name,String mobile) throws Exception{
        try {
            //throw exception if there
            if(userHashMap.containsKey(mobile)) throw new Exception("User already exists");

            // created obj
            User user = new User();
            user.setMobile(mobile);
            user.setName(name);

            // related maps added
            userHashMap.put(mobile,user);
            userMessageDb.put(user,new ArrayList<>());
        }
        catch (Exception e){

        }

        //success
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){

        Group group = new Group();

        try {
            // member should be >2
            int NoOfUsers = users.size();
            if(NoOfUsers<2) throw new Exception();

            //grp name
            String groupName;
            if(NoOfUsers==2){
                groupName = users.get(1).getName();
            }
            else {
                groupCount++;
                groupName = "Group "+String.valueOf(groupCount);
            }

            // created grp
            group.setName(groupName);
            group.setNumberOfParticipants(NoOfUsers);

            //creating admin
            User admin = users.get(0);

            // alloting grp to users
        /*for(User user : users){
            if(userGroupDb.containsKey(user.getName()) && userGroupDb.get(user.getName())!=null){
                return null;
            }
            else{
                userGroupDb.put(user.getName(),groupName);
            }
        }*/

            // setting group maps
            groupHashMap.put(groupName,group);
            groupUsersDb.put(group,users);
            groupAdminDb.put(group,admin);
            groupMessageDb.put(group,new ArrayList<>());
        }
        catch (Exception e){

        }

        return group;
    }

    public int createMessage(String content){
        int messageCount = messageHashMap.size()+1;
        Date msgDate = new Date();

        Message message = new Message(messageCount,content);
        message.setTimestamp(msgDate);

        messageHashMap.put(messageCount,message);
        return messageCount;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{

        try {
            if(!groupHashMap.containsKey(group) || !groupMessageDb.containsKey(group)){
                throw new Exception("Group does not exist");
            }
            //if( !userGroupDb.containsKey(sender.getName()) || !userGroupDb.get(sender.getName()).equals(groupName)) throw new Exception("You are not allowed to send message");

            boolean present = false;

            for(User user : groupUsersDb.get(group)){
                if(user.equals(sender)){
                    present=true;
                    break;
                }
            }

            if(present==false){
                throw new Exception("You are not allowed to send message");
            }

            List<Integer> userMessages = userMessageDb.get(sender);
            userMessages.add(message.getId());
            userMessageDb.put(sender,userMessages);

            List<Integer> groupMessages = groupMessageDb.get(group);
            groupMessages.add(message.getId());
            groupMessageDb.put(group,groupMessages);

            messageHashMap.put(message.getId(),message);

            return groupMessageDb.get(group).size();
        }
        catch (Exception e){
            return 0;
        }
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{

        try {
            if(!groupHashMap.containsKey(group) || !groupUsersDb.containsKey(group)){
                throw new Exception("Group does not exist");
            }

            if(!groupAdminDb.get(group).equals(approver)){
                throw new Exception("Approver does not have rights");
            }

            //if( !userGroupDb.containsKey(user.getName()) || !userGroupDb.get(user.getName()).equals(groupName)) throw new Exception("Approver does not have rights");

            //if( !userGroupDb.containsKey(user.getName()) || !userGroupDb.get(user.getName()).equals(groupName)) throw new Exception("User is not a participant");

            boolean present = false;
            for(User grpUser : groupUsersDb.get(group)){
                if(grpUser.equals(user)) present=true;
            }

            if(present==false){
                throw new Exception("User is not a participant");
            }

            groupAdminDb.put(group,user);
        }
        catch (Exception e){

        }

        return "SUCCESS";

    }

    public int removeUser(User user) throws Exception{

        Group group = null;

        try {
            if (user.getName() == null || !userHashMap.containsKey(user.getName())) {
                throw new Exception("User not found");
            }

            for (Group grp : groupUsersDb.keySet()) {
                for (User grpUser : groupUsersDb.get(grp)) {
                    if (grpUser.equals(user)) {
                        group = grp;
                        break;
                    }
                }
                if (group != null) {
                    break;
                }
            }

            if (group == null) {
                throw new Exception("User not found");
            }
        }
        catch(Exception e){
            return 0;
        }

        try{

            if(groupAdminDb.get(group).equals(user)){
                throw new Exception("Cannot remove admin");
            }

            List<Integer> messagesList = userMessageDb.get(user);

            List<Integer> messagesInGroup = groupMessageDb.get(group);
            for(int msgId : messagesList){
                if(messagesInGroup.contains(msgId)) messagesInGroup.remove(msgId);
                if(messageHashMap.containsKey(msgId)) messageHashMap.remove(msgId);
            }

            groupMessageDb.put(group,messagesInGroup);
            userMessageDb.put(user,new ArrayList<>());

            List<User> userList = groupUsersDb.get(group);
            if(userList.contains(user)) userList.remove(user);
            groupUsersDb.put(group,userList);

            group.setNumberOfParticipants(userList.size());
            groupHashMap.put(group.getName(), group);

        }
        catch (Exception e){

        }
        int updatedNoUsers = group.getNumberOfParticipants();
        int updatedNoOfMsg = groupMessageDb.get(group).size();
        int noOfAllmsg = messageHashMap.size();

        return updatedNoUsers+updatedNoOfMsg+noOfAllmsg;

    }
    public String findMessage(Date start, Date end, int K) throws Exception {

        int count = 0;

        for (Message msg : messageHashMap.values()) {
            Date msgDate = msg.getTimestamp();
            if (msgDate.equals(start) || (msgDate.after(start) && msgDate.before(end)) || msgDate.equals(end)) {
                if (count == K) return msg.getContent();

                count++;
            }
        }

        if (count < K) throw new Exception("K is greater than the number of messages");

        return null;
    }


}
