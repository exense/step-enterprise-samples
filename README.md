# step-enterprise-samples
This project demonstrates the usage of the advanced fonctionalities of the entreprise version of Step

## Usage
First of all, you will need the [enterprise version of Step](https://step.exense.ch/pricing/). Do not hesitate to [contact us](https://step.exense.ch/contact/) for more information.

### Java samples

In order to use the Java samples, you will then be able to connect to our private nexus by configuring your maven settings.xml as follow:

```xml
<servers>
	<server>
		<id>nexus-exense</id>
		<username>your_step_enterprise_username</username>
		<password>password</password>
	</server>
</servers>
<!-- if your compagny uses a proxy, add these lines: -->
<proxies>
	<proxy>
		<id>step-proxy</id>
		<active>true</active>
		<protocol>http</protocol>
		<host>your_proxy</host>
		<port>your_proxy_port</port>
		<nonProxyHosts>*.yourdomain</nonProxyHosts>
	</proxy>
</proxies>
```
You will them be able to compile the project and test how to use our differents plugins and advanced API.
