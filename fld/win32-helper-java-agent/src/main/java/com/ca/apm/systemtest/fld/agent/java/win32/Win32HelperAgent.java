package com.ca.apm.systemtest.fld.agent.java.win32;

import java.lang.instrument.Instrumentation;
import java.nio.Buffer;
import java.nio.CharBuffer;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;

/**
 * This is a very simple Java agent that allows us to set Win32 console window title.
 */
public class Win32HelperAgent {

    /**
     * To .NET's Console.Title documentation, this is the limit of Win32 console title length.
     */
    public static final int MAX_CONSOLE_TITLE_LENGTH = 24500;

    @SuppressWarnings("unused")
    public static void agentmain(String agentArgs, Instrumentation inst) {
        String title = System.getProperty("win32.console.title");
        if (title != null) {
            CharBuffer oldTitleBuffer = CharBuffer.allocate(MAX_CONSOLE_TITLE_LENGTH);
            int oldTitleSize = 0;
            try {
                oldTitleSize = Kernel32Lib.INSTANCE
                    .GetConsoleTitle(oldTitleBuffer, oldTitleBuffer.capacity());
            } catch (LastErrorException e) {
                System.err.printf("GetConsoleTitle() failed: %d", e.getErrorCode());
            }
            String oldTitle = new String(oldTitleBuffer.array(), 0, oldTitleSize);

            try {
                String newTitle = title + oldTitle;
                Kernel32Lib.INSTANCE.SetConsoleTitle(
                    newTitle.substring(0, Math.min(newTitle.length(), MAX_CONSOLE_TITLE_LENGTH)));
            } catch (LastErrorException e) {
                System.err.printf("SetConsoleTitle(\"%s\") failed: %d", title, e.getErrorCode());
            }
        }

        String hideWindowStr = System.getProperty("win32.console.hide");
        WinDef.HWND consoleWindowHwnd = null;
        if (hideWindowStr != null
            && Boolean.valueOf(hideWindowStr)
            && (consoleWindowHwnd = Kernel32.INSTANCE.GetConsoleWindow()) != null) {
            if (!User32.INSTANCE.ShowWindow(consoleWindowHwnd, WinUser.SW_HIDE)) {
                System.err.printf("Failed to hide console window. ShowWindow failed: %d",
                    Native.getLastError());
            }
        }
    }

    @SuppressWarnings("unused")
    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

    public interface Kernel32Lib extends Library {
        Kernel32Lib INSTANCE = (Kernel32Lib) Native.loadLibrary("kernel32", Kernel32Lib.class,
            W32APIOptions.UNICODE_OPTIONS);

        void SetConsoleTitle(String title) throws LastErrorException;

        /**
         * DWORD WINAPI GetConsoleTitle(
         * _Out_ LPTSTR lpConsoleTitle,
         * _In_  DWORD  nSize
         * );
         */
        int GetConsoleTitle(Buffer lpConsoleTitle, int nSize) throws LastErrorException;
    }
}

