using StepApi;
using Microsoft.Office.Interop.Outlook;
using ScriptDev;
using NUnit.Framework;
using System.Collections.Generic;

namespace OfficeKeywords
{
    public class Keywords : StepApi.AbstractScript
    {
        [Keyword]
        public void OpenOutlook()
        {
            Application application = null;
            
            application = new Application();
        }
    }

    public class KeywordsTests
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
        public void OpenChromeTest()
        {
            var output = runner.run("Open Chrome", @"{}", new Dictionary<string, string>() { { "headless", @"false" } });
            
            Assert.AreEqual("www.exense.ch/", (string)output.payload["exense: Home"]);
        }
    }
}
