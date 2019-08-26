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
            Assert.IsNull(Output.Error, (Output.Error == null) ? "" : "Error was: " + Output.Error.Msg);

            Output = Runner.Run("Search in google", @"{search:'exense'}");
            Assert.IsNull(Output.Error, (Output.Error == null) ? "" : "Error was: " + Output.Error.Msg);
            Assert.AreEqual("https://www.exense.ch", (string)Output.Payload["exense: Home"]);
        }
    }
}
