package com.iam1xd.gfx;

import android.os.RemoteException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import kotlin.NotImplementedError;

public final class UserService extends IUserService.Stub {


    @Override
    public void destroy() {
        throw new NotImplementedError("An operation is not implemented: Not yet implemented");
    }

    @Override
    public void exit() throws RemoteException {

    }

    @Override
    public void executeAdbCommands(String command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public String executeAdbCommand2(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            bufferedReader.close();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return output.toString();
            } else {
                return "Command failed with exit code: " + exitCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
