using System;
using System.Collections.Generic;
using OpenQA.Selenium;
using OpenQA.Selenium.Chrome;
using Step.Grid.IO;
using Step.Handlers.NetHandler;

namespace SeleniumTest
{
    public class Keywords : AbstractKeyword
    {
        [Keyword(Name = "Open Chrome")]
        public void OpenChrome()
        {
            ChromeDriver driver = new ChromeDriver();

            ChromeOptions options = new ChromeOptions();
            bool headless = Boolean.Parse(Properties["headless"]);
            if (headless)
            {
                options.AddArguments(new string[] { "headless", "disable-infobars", "disable-gpu", "no-sandbox" });
            }

            driver.Manage().Timeouts().ImplicitWait = TimeSpan.FromSeconds(10);

            Session.Put("driver", new Wrapper(driver));
        }

        [Keyword(Name = "Navigate to URL")]
        public void Navigate()
        {
            IWebDriver driver = GetDriver();
            Output.StartMeasure("Navigate");
            driver.Url = (string)Properties["url_exense"];
            Output.StopMeasure();
        }

        [Keyword(Name = "Search in google")]
        public void SearchInGoogle()
        {
            IWebDriver driver = GetDriver();

            if (Input["search"] != null)
            {
                if (driver == null)
                {
                    throw new Exception("Please first execute keyword \"Open Chome\" in order to have a driver available for this keyword");
                }

                driver.Url = "http://www.google.com";

                IWebElement searchInput = driver.FindElement(By.Name("q"));

                String searchString = (string)Input["search"];
                searchInput.SendKeys(searchString + Keys.Enter);

                IWebElement resultCountDiv = driver.FindElement(By.XPath("//div/nobr"));

                ICollection<IWebElement> resultHeaders = driver.FindElements(By.XPath("//div[@class='r']//h3"));
                foreach (IWebElement result in resultHeaders)
                {
                    Output.Add(result.Text, result.FindElement(By.XPath("..//cite")).Text);
                }
                Screenshot ss = ((ITakesScreenshot)driver).GetScreenshot();

                string screenshot = ss.AsBase64EncodedString;
                byte[] screenshotAsByteArray = ss.AsByteArray;
                Output.AddAttachment(AttachmentHelper.GenerateAttachmentFromByteArray(screenshotAsByteArray,"screenshot.png"));
            }
            else
            {
                throw new Exception("Input parameter 'search' not defined");
            }
        }


        private IWebDriver GetDriver()
        {
            Wrapper wrapper = (Wrapper)Session.Get("driver");

            IWebDriver driver = wrapper.driver;
            return driver;
        }
    }
}
