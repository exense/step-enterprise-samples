using System;
using System.Collections.Generic;
using OpenQA.Selenium;
using OpenQA.Selenium.Chrome;
using SeleniumCommons;
using StepApi;

namespace SeleniumTest
{
    public class Keywords : AbstractKeyword
    {
        [Keyword(name = "Open Chrome and search in Google")]
        public void OpenChromeAndSearchInGoogle()
        {
            ChromeDriver driver = createDriver();
            googleSearch(driver);
            driver.Quit();
        }


        [Keyword(name = "Open Chrome")]
        public void OpenChrome()
        {
            ChromeDriver driver = createDriver();

            session.put("driver", new Wrapper(driver));
        }

        private ChromeDriver createDriver()
        {
            ChromeDriver driver = new ChromeDriver();

            ChromeOptions options = new ChromeOptions();
            if (properties.ContainsKey("headless") && Boolean.Parse(properties["headless"]))
            {
                options.AddArguments(new string[] { "headless", "disable-infobars", "disable-gpu", "no-sandbox" });
            }

            driver.Manage().Timeouts().ImplicitWait = TimeSpan.FromSeconds(10);

            return driver;
        }

        [Keyword(name = "Navigate to URL")]
        public void Navigate()
        {
            IWebDriver driver = GetDriver();
            output.StartMeasure("Navigate");
            driver.Url = (string)properties["url_exense"];
            output.StopMeasure();
        }

        [Keyword(name = "Search in google")]
        public void SearchInGoogle()
        {
            IWebDriver driver = GetDriver();

            googleSearch(driver);
        }

        private void googleSearch(IWebDriver driver)
        {
            if (input["search"] != null)
            {
                if (driver == null)
                {
                    throw new Exception("Please first execute keyword \"Open Chome\" in order to have a driver available for this keyword");
                }

                driver.Url = "http://www.google.com";
                
                IWebElement searchInput = driver.FindElement(By.XPath("//input[@name='q']"));

                IWebElement searchInput = driver.FindElement(By.Name("q"));

                String searchString = (string)input["search"];
                searchInput.SendKeys(searchString + Keys.Enter);

                IWebElement resultCountDiv = driver.FindElement(By.XPath("//div/nobr"));

                ICollection<IWebElement> resultHeaders = driver.FindElements(By.XPath("//div[@class='r']//h3"));
                foreach (IWebElement result in resultHeaders)
                {
                    output.Add(result.Text, result.FindElement(By.XPath("..//cite")).Text);
                }
                Screenshot ss = ((ITakesScreenshot)driver).GetScreenshot();

                string screenshot = ss.AsBase64EncodedString;
                byte[] screenshotAsByteArray = ss.AsByteArray;
                output.AddAttachment(AttachmentHelper.GenerateAttachmentFromByteArray(screenshotAsByteArray,"screenshot.png"));
            }
            else
            {
                throw new Exception("Input parameter 'search' not defined");
            }
        }

        private IWebDriver GetDriver()
        {
            Wrapper wrapper = (Wrapper)session.Get("driver");

            IWebDriver driver = wrapper.driver;
            return driver;
        }
    }
}
