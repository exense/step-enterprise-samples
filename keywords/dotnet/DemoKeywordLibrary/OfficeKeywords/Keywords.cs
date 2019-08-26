using NUnit.Framework;
using Outlook = Microsoft.Office.Interop.Outlook;
using System.Diagnostics;
using System.Linq;
using System;
using Step.Handlers.NetHandler;
using Step.Functions.IO;

namespace OfficeTest
{
    public class Keywords : AbstractKeyword
    {
        private Outlook.Application GetApplication()
        {
            Type outlookType = Type.GetTypeFromProgID("Outlook.Application");

            if (outlookType == null)
            {
                return null;
            }
            return (Outlook.Application)Activator.CreateInstance(outlookType);
        }

        [Keyword]
        public void StartOutlook()
        {
            if (Process.GetProcessesByName("OUTLOOK").Count() == 0)
            {
                Outlook.Application outlook;
                if ((outlook = GetApplication()) == null)
                {
                    Output.SetBusinessError("Outlook seems to not be installed on this machine. Aborting");
                    return;
                }

                Outlook.MAPIFolder inbox =
                    outlook.GetNamespace("MAPI").GetDefaultFolder(Outlook.OlDefaultFolders.olFolderInbox);

                inbox.Display();
            }
        }

        [Keyword]
        public void ReadEmails()
        {
            string search = Input["search"].ToString();

            Outlook.Application outlook;
            if ((outlook = GetApplication()) == null)
            {
                Output.SetBusinessError("Outlook seems to not be installed on this machine. Aborting");
                return;
            }

            Outlook.MAPIFolder inbox =
                outlook.Session.GetDefaultFolder(Outlook.OlDefaultFolders.olFolderInbox);

            foreach (Outlook.MailItem item in inbox.Items.Restrict("[Unread]=true").OfType<Outlook.MailItem>().
                Where(m => m.Subject.Contains(search)).OrderByDescending(m => m.CreationTime))
            {
                item.Display();

                item.UnRead = false;

                item.FlagIcon = Outlook.OlFlagIcon.olBlueFlagIcon;
                item.Categories = "Blue Category";

                item.Close(Outlook.OlInspectorClose.olSave);
            }
        }

        bool received = false;
        private void MailReceived()
        {
            received = true;
        }

        [Keyword]
        public void SendEmail()
        {
            Outlook.Application outlook;
            if ((outlook = GetApplication()) == null)
            {
                Output.SetBusinessError("Outlook seems to not be installed on this machine. Aborting");
                return;
            }

            outlook.NewMail += new Outlook.ApplicationEvents_11_NewMailEventHandler(MailReceived);
            received = false;
            Outlook.MailItem mail = outlook.CreateItem(Outlook.OlItemType.olMailItem);

            mail.Display();

            mail.To = outlook.Session.CurrentUser.Address;

            mail.Subject = Input["subject"].ToString();

            mail.Body = "This is a test";

            mail.Send();

            while (!received) System.Threading.Thread.Sleep(500);
        }
    }

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
        public void SendEmailTest()
        {
            Output = Runner.Run("StartOutlook");
            Assert.IsNull(Output.Error, (Output.Error == null) ? "" : "Error was: " + Output.Error.Msg);

            Output = Runner.Run("SendEmail", "{subject:'This is a test - email 1'}");
            Assert.IsNull(Output.Error, (Output.Error == null) ? "" : "Error was: " + Output.Error.Msg);

            Output = Runner.Run("SendEmail", "{subject:'This is a test - email 2'}");
            Assert.IsNull(Output.Error, (Output.Error == null) ? "" : "Error was: " + Output.Error.Msg);

            Output = Runner.Run("SendEmail", "{subject:'This is a test - email 3'}");
            Assert.IsNull(Output.Error, (Output.Error == null) ? "" : "Error was: " + Output.Error.Msg);

            Output = Runner.Run("ReadEmails", "{search:'This is a test'}");
            Assert.IsNull(Output.Error, (Output.Error == null) ? "" : "Error was: " + Output.Error.Msg);
        }
    }
}