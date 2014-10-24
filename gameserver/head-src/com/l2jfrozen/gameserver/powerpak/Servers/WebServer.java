package com.l2jfrozen.gameserver.powerpak.Servers;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.L2Properties;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer extends Thread
{
	private static WebServer _instance;

	public static WebServer getInstance()
	{
		if(_instance == null)
			_instance = new WebServer();
		return _instance;
	}

	private HttpServer _server;
	protected static final Logger LOGGER = Logger.getLogger(WebServer.class);

	private WebServer()
	{
		if(PowerPakConfig.WEBSERVER_ENABLED)
			try
			{
				int handlers = 0;
				_server = HttpServer.create(new InetSocketAddress(InetAddress.getByName(PowerPakConfig.WEBSERVER_HOST), PowerPakConfig.WEBSERVER_PORT), 10);
				L2Properties p = new L2Properties("./config/powerpak/websevices.properties");
				for(Object s : p.keySet())
				{
					String contextHandlerName = p.getProperty(s.toString());
					try
					{
						Class<?> contextHandler = Class.forName(contextHandlerName);
						if(contextHandler != null && HttpHandler.class.isAssignableFrom(contextHandler))
						{
							_server.createContext(s.toString(), (HttpHandler) contextHandler.newInstance());
							handlers++;
						}
					}
					catch(Exception e)
					{
						if(Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();

						LOGGER.warn( "WebServer: Error while creating handler " + contextHandlerName + " for '" + s.toString() + "': " + e);
						continue;
					}

				}
				if(handlers > 0)
				{
					_server.start();
					Runtime.getRuntime().addShutdownHook(this);
					LOGGER.info("WebServer: Listen at " + PowerPakConfig.WEBSERVER_HOST + ":" + PowerPakConfig.WEBSERVER_PORT);
					LOGGER.info("WebServer: " + handlers + " context handler(s) registred");
				}
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();

				LOGGER.warn( "WebServer: Error " + e + " while staring");
				_server = null;
			}
	}

	@Override
	public void run()
	{
		if(_server != null)
		{
			LOGGER.info("WebServer: stopped");
			_server.stop(0);
			_server = null;
		}
	}
}
