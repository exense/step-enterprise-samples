using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using OpenQA.Selenium;
using StepApi;

namespace SeleniumCommons
{
    public class Wrapper : Closeable
    {
        public IWebDriver driver;

        public Wrapper(IWebDriver driver)
        {
            this.driver = driver;
        }

        public void close()
        {
            if (driver != null)
            {
                driver.Quit();
            }
        }
    }
}
