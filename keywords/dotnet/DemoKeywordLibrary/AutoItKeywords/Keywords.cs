using AutoIt;
using NUnit.Framework;
using ScriptDev;
using StepApi;
using System;
using System.Diagnostics;
using System.Threading;

namespace AutoItKeywords
{
    public class Keywords : StepApi.AbstractScript
    {
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
            IntPtr winHandle;
            int pid = AutoItX.Run("notepad.exe", ".");
            winHandle = GetProcessHandel(pid);

            if (AutoItX.WinWaitActive(winHandle, timeout: 10) != 1)
            {
                output.setError("Error waiting for the Notepad window .");
                return;
            }

            AutoItX.Send("This is an AutoIt test using step");

            if (AutoItX.WinKill(winHandle) != 1)
            {
                output.setError("Error closing the Notepad window");
                return;
            }

            if (AutoItX.WinWaitClose(winHandle, timeout: 10) != 1)
            {
                output.setError("Error waiting for the Notepad window to close");
                return;
            }
        }
    }

    public class AutoItKeywordsTests
    {
        ScriptRunner runner;

        [SetUp]
        public void Init()
        {
            runner = new ScriptRunner(typeof(Keywords));
        }

        [TearDown]
        public void tearDown()
        {
            runner.close();
        }

        [TestCase()]
        public void NotepadTest()
        {
            var output = runner.run("Open Notepad, edit and close", @"{}");
            Assert.IsNull(output.error, (output.error == null) ? "" : "Error was: " + output.error.msg);
        }
    }
}
