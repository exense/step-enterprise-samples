using AutoIt;
using NUnit.Framework;
using ScriptDev;
using StepApi;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace AutoItKeywords
{
    public class Keywords : StepApi.AbstractScript
    {
        [Keyword(name = "Open Notepad, edit and close")]
        public void EditInNotepad()
        {
            int pid = 0;
            if ((pid = AutoItX.Run("notepad.exe", ".")) == 0)
            {
                output.setError("Error stating Notepad");
                return;
            }
            AutoItX.Ge
            if (AutoItX.WinWaitActive("Untitled", timeout: 10) != 1)
            {
                output.setError("Error waiting for the Notepad window");
                return;
            }

            AutoItX.Send("I'm in notepad");

            //Thread.Sleep(100000);

            if (AutoItX.WinKill("*Untitled") != 1)
            {
                output.setError("Error closing the Notepad window");
                return;
            }

            if (AutoItX.WinWaitClose("*Untitled", timeout: 10) != 1)
            {
                output.setError("Error waiting for the Notepad window to close");
                return;
            }

            if (AutoItX.ProcessWaitClose(pid.ToString(), timeout: 10) != 1)
            {
                output.setError("Error waiting for the Notepad process to close");
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
        public void AutoItTest()
        {
            var output = runner.run("Open Notepad, edit and close", @"{}");
            Assert.Null(output.error);
        }
    }
}
