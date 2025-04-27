package com.iam1xd.gfx;

interface IUserService {

    void destroy() = 16777114; // Destroy method defined by Shizuku server

    void exit() = 1; // Exit method defined by user

    void executeAdbCommands(String command) = 2;
    String executeAdbCommand2(String command) = 4;
}