package listeners.sumCharackteristics;

import handlers.SummarizeHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import views.windows.Command;
import views.windows.SummarizeWindow;

public class SetCommandListener implements ActionListener {

    private SummarizeHandler sumCharackteristicsHandler;
    private Command command;

    public SetCommandListener(
            SummarizeHandler sumCharackteristicsHandler,
            Command command) {

        super();

        if (sumCharackteristicsHandler == null || command == null) {
            throw new IllegalArgumentException("Arguments must not be null!");
        }

        this.sumCharackteristicsHandler = sumCharackteristicsHandler;
        this.command = command;
    }

    public void actionPerformed(ActionEvent e) {

        try {

            this.sumCharackteristicsHandler.setCommand(this.command);

        } catch (Exception e1) {

            e1.printStackTrace();

        }
    }
}
