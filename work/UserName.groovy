package jisis.UserName

import org.unesco.jisis.corelib.client.ConnectionPool;


import org.unesco.jisis.corelib.common.UserInfo;


import org.unesco.jisis.corelib.exceptions.DbException;

def UserName() {

def connection = ConnectionPool.getDefaultConnection();
def userInfo = connection.getUserInfo();

def userName = userInfo.getUserName();

return userName;
}
UserName()