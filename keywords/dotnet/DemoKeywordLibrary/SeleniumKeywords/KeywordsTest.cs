using NUnit.Framework;
using Step.Functions.IO;
using Step.Handlers.NetHandler;
using System.Collections.Generic;

namespace SeleniumTest
{
    public class KeywordsTests
    {
        ExecutionContext Runner;
        Output Output;

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
        public void OpenChromeTest()
        {
            Output = Runner.Run("Open Chrome", @"{}", new Dictionary<string, string>() { { "headless", @"false" } });
            Assert.IsNull(Output.error, (Output.error == null) ? "" : "Error was: " + Output.error.msg);

            Output = Runner.Run("Search in google", @"{search:'exense'}");
            Assert.IsNull(Output.error, (Output.error == null) ? "" : "Error was: " + Output.error.msg);
            Assert.AreEqual("https://www.exense.ch", (string)Output.payload["exense: Home"]);
        }
    }
}
