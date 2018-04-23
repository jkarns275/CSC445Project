package server.workers;

import networking.headers.LeaveHeader;

public class LeaveWorker implements Runnable {
    LeaveHeader leaveHeader;

    public LeaveWorker(LeaveHeader leaveHeader) {
        this.leaveHeader = leaveHeader;
    }

    public void run() {

    }
}
