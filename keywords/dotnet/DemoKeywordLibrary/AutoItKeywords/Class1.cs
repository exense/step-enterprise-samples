using AutoIt;
using NUnit.Framework;
using ScriptDev;
using StepApi;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AutoItKeywords
{
    public class AutoItKeywords
    {
        [Keyword(name = "Open Notepad, edit and close")]
        public void EditInNotepad()
        {
            AutoItX.Run("notepad.exe", ".");
            AutoItX.WinWaitActive("Untitled");
            AutoItX.Send("I'm in notepad");
            IntPtr winHandle = AutoItX.WinGetHandle("Untitled");
            AutoItX.WinKill(winHandle);
        }
    }

    public class AutoItKeywordsTests
    {
        ScriptRunner runner;

        [SetUp]
        public void Init()
        {
            runner = new ScriptRunner(typeof(AutoItKeywords));
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
