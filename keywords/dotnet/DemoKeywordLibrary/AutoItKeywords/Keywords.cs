﻿using AutoIt;
using log4net;
using NUnit.Framework;
using Step.Handlers.NetHandler;
using System;
using System.Diagnostics;

namespace AutoItTest
{
    public class Keywords : AbstractKeyword
    {
        protected static readonly ILog logger = LogManager.GetLogger(typeof(Keywords));

        private IntPtr GetProcessHandel(int pid)
        {
            Process proc;
            try
            {
                proc = Process.GetProcessById(pid);
            } catch (Exception)
            {
                return new IntPtr(0);
            }
            // wait for the win handle:
            if (!proc.WaitForInputIdle(milliseconds : 10000))
            {
                return new IntPtr(0);
            }
            return proc.MainWindowHandle;
        }

        [Keyword(name = "Open Notepad, edit and close")]
        public void EditInNotepad()
        {
            logger.Info("Executing keyword 'EditInNotepad'");

            IntPtr winHandle;
            int pid = AutoItX.Run("notepad.exe", ".");
            winHandle = GetProcessHandel(pid);

            if (AutoItX.WinWaitActive(winHandle, timeout: 10) != 1)
            {
                output.SetError("Error waiting for the Notepad window .");
                return;
            }

            AutoItX.Send("This is an AutoIt test using step");

            if (AutoItX.WinKill(winHandle) != 1)
            {
                output.SetError("Error closing the Notepad window");
                return;
            }

            if (AutoItX.WinWaitClose(winHandle, timeout: 10) != 1)
            {
                output.SetError("Error waiting for the Notepad window to close");
                return;
            }
        }
    }

    public class AutoItKeywordsTests
    {
        ExecutionContext Runner;

        [SetUp]
        public void Init()
        {
            Runner = KeywordRunner.GetExecutionContext(typeof(Keywords));
        }

        [TearDown]
        public void TearDown()
        {
            Runner.Close();
        }

        [TestCase()]
        public void NotepadTest()
        {
            var output = Runner.Run("Open Notepad, edit and close", @"{}");
            Assert.IsNull(output.error, (output.error == null) ? "" : "Error was: " + output.error.msg);
        }
    }
}
