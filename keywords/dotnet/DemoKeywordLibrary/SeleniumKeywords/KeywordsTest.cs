using NUnit.Framework;
using ScriptDev;
using STEP;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TemplateSeleniumLibrary
{
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
            Assert.IsNull(output.error, (output.error == null) ? "" : "Error was: " + output.error.msg);

            output = runner.run("Search in google", @"{search:'exense'}");
            Assert.IsNull(output.error, (output.error == null) ? "" : "Error was: " + output.error.msg);
            Assert.AreEqual("https://www.exense.ch/", (string)output.payload["exense: Home"]);
        }
    }
}
