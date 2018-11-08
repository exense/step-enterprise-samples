using System;
using System.Collections.Generic;
using OpenQA.Selenium;
using OpenQA.Selenium.Chrome;
using SeleniumCommons;
using StepApi;

namespace STEP
{
    public class Keywords : StepApi.AbstractScript
    {
        [Keyword(name = "Open Chrome")]
        public void OpenChrome()
        {
            ChromeDriver driver = new ChromeDriver();

            ChromeOptions options = new ChromeOptions();
            bool headless = Boolean.Parse(properties["headless"]);
            if (headless)
            {
                options.AddArguments(new string[] { "headless", "disable-infobars", "disable-gpu", "no-sandbox" });
            }

            driver.Manage().Timeouts().ImplicitWait = TimeSpan.FromSeconds(10);

            session.put("driver", new Wrapper(driver));
        }

        [Keyword(name = "Navigate to URL")]
        public void Navigate()
        {
            IWebDriver driver = getDriver();
            output.startMeasure("Navigate");
            driver.Url = (string)properties["url_exense"];
            output.stopMeasure();
        }

        [Keyword(name = "Search in google")]
        public void SearchInGoogle()
        {
            IWebDriver driver = getDriver();

            if (input["search"] != null)
            {
                if (driver == null)
                {
                    throw new Exception("Please first execute keyword \"Open Chome\" in order to have a driver available for this keyword");
                }

                driver.Url = "http://www.google.com";

                IWebElement searchInput = driver.FindElement(By.Id("lst-ib"));

                String searchString = (string)input["search"];
                searchInput.SendKeys(searchString + Keys.Enter);

                IWebElement resultCountDiv = driver.FindElement(By.XPath("//div/nobr"));

                ICollection<IWebElement> resultHeaders = driver.FindElements(By.XPath("//div[@class='r']//h3"));
                foreach (IWebElement result in resultHeaders)
                {
                    output.add(result.Text, result.FindElement(By.XPath("..//cite")).Text);
                }

            }
            else
            {
                throw new Exception("Input parameter 'search' not defined");
            }
        }


        private IWebDriver getDriver()
        {
            Wrapper wrapper = (Wrapper)session.get("driver");

            IWebDriver driver = wrapper.driver;
            return driver;
        }
    }
}
