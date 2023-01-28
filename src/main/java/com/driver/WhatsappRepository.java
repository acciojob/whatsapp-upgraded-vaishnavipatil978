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
    HashMap<String, List<Integer>> userMessageDb;
    HashMap<String,String> userGroupDb;

    //Group
    HashMap<String,Group> groupHashMap;
    HashMap<String,String> groupAdminDb;
    HashMap<String,List<User>> groupUsersDb;
    HashMap<String ,List<Integer>> groupMessageDb;

    //Message
    HashMap<Integer,Message> messageHashMap;
    int groupCount ;

    public WhatsappRepository(){

        userHashMap = new HashMap<>();
        userMessageDb = new HashMap<>();
        userGroupDb = new HashMap<>();

        groupHashMap = new HashMap<>();
        groupUsersDb = new HashMap<>();
        groupAdminDb = new HashMap<>();
        groupMessageDb = new HashMap<>();

        messageHashMap = new HashMap<>();
        groupCount=0;
    }

    public String createUser(String name,String mobile) throws Exception{
        if(userHashMap.containsKey(mobile)) throw new Exception("User already exists");

        User user = new User(name, mobile);
        userHashMap.put(mobile,user);
        userMessageDb.put(name,new ArrayList<>());
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){

        if(users.size()<2) return  null;

        int NoOfUsers = users.size();
        String groupName;

        if(NoOfUsers==2){
            groupName = users.get(1).getName();
        }
        else {
            groupCount++;
            groupName = "Group "+String.valueOf(groupCount);
        }

        Group group = new Group(groupName, NoOfUsers);

        String admin = users.get(0).getName();

        for(User user : users){
            if(userGroupDb.containsKey(user.getName()) && userGroupDb.get(user.getName())!=null){
                return null;
            }
            else{
                userGroupDb.put(user.getName(),groupName);
            }
        }

        groupHashMap.put(groupName, group);
        groupUsersDb.put(groupName,users);
        groupAdminDb.put(groupName,admin);
        groupMessageDb.put(groupName,new ArrayList<>());

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
        String groupName = group.getName();

        if(groupName==null || !groupHashMap.containsKey(groupName)) throw new Exception("Group does not exist");

        if( !userGroupDb.containsKey(sender.getName()) || !userGroupDb.get(sender.getName()).equals(groupName)) throw new Exception("You are not allowed to send message");

        String userName = sender.getName();

        List<Integer> userMessages = userMessageDb.get(userName);
        userMessages.add(message.getId());
        userMessageDb.put(userName,userMessages);

        List<Integer> groupMessages = groupMessageDb.get(groupName);
        groupMessages.add(message.getId());
        groupMessageDb.put(groupName,groupMessages);

        return groupMessages.size();

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{

        String groupName = group.getName();

        if(!groupHashMap.containsKey(groupName)) throw new Exception("Group does not exist");

        if(!groupAdminDb.get(groupName).equals(approver.getName())) throw new Exception("Approver does not have rights");

        //if( !userGroupDb.containsKey(user.getName()) || !userGroupDb.get(user.getName()).equals(groupName)) throw new Exception("Approver does not have rights");

        if( !userGroupDb.containsKey(user.getName()) || !userGroupDb.get(user.getName()).equals(groupName)) throw new Exception("User is not a participant");

        groupAdminDb.put(groupName,user.getName());

        return "SUCCESS";

    }

    public int removeUser(User user) throws Exception{

        String username = user.getName();

        if(username==null || !userGroupDb.containsKey(username) ) throw new Exception("User not found");

        String groupname = userGroupDb.get(username);

        if(groupname==null || groupAdminDb.get(groupname).equals(username)) throw new Exception("Cannot remove admin");

        userGroupDb.remove(username);
        List<Integer> messagesList = userMessageDb.get(username);

        List<Integer> messagesInGroup = groupMessageDb.get(groupname);
        for(int msgId : messagesList){
            if(messagesInGroup.contains(msgId)) messagesInGroup.remove(msgId);
            if(messageHashMap.containsKey(msgId)) messageHashMap.remove(msgId);
        }

        groupMessageDb.put(groupname,messagesInGroup);
        userMessageDb.remove(username);

        List<User> userList = groupUsersDb.get(groupname);
        userList.remove(user);
        groupUsersDb.put(groupname,userList);

        Group group = groupHashMap.get(groupname);
        group.setNumberOfParticipants(userList.size());
        groupHashMap.put(groupname,group);

        return group.getNumberOfParticipants()+messagesInGroup.size()+messageHashMap.size();
    }

    public String findMessage(Date start, Date end, int K) throws Exception{

        int count = 0;

        for(Message msg : messageHashMap.values()){
            Date msgDate = msg.getTimestamp();
            if(msgDate.equals(start) || (msgDate.after(start) && msgDate.before(end)) || msgDate.equals(end)){
                if(count==K) return msg.getContent();

                count++;
            }
        }

        if(count<K) throw new Exception("K is greater than the number of messages");

        return null;

    }


}
