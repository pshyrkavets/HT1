package app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ManagePersonServlet extends HttpServlet {
	
	// identifier for serialization/deserialization
	private static final long serialVersionUID = 1L;
	
	// main object for keeping the data of a phonebook
	private Phonebook phonebook;
       
    public ManagePersonServlet()
    {
    	// calls parent constructor
    	super();
		
    	// creation of an exemplar of the phonebook
    	try
		{
			this.phonebook = Phonebook.getInstance();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}        
        
    }

    // validation of first name, surname, middle name and generation of a message in case of invalid data
    private String validatePersonFMLName(Person person)
    {
		String error_message = "";
		
		if (!person.validateFMLNamePart(person.getName(), false))
		{
			error_message += "Name should be a String from 1 to 150 characters, digits, the underline signs(_) or the negative signs(-).<br />";
		}
		
		if (!person.validateFMLNamePart(person.getSurname(), false))
		{
			error_message += "Surname should be a String from 1 to 150 characters, digits, the underline signs(_) or the negative signs(-).<br />";
		}
		
		if (!person.validateFMLNamePart(person.getMiddlename(), true))
		{
			error_message += "Middle name should be a String from 0 to 150 characters, digits, the underline signs(_) or the negative signs(-).<br />";
		}
		
		return error_message;
    }
    
    // reaction on get requests
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// It is mandatory to switch to UTF-8 before a request to any parameter because the Russian language is not appropriate for get/post requests.
		request.setCharacterEncoding("UTF-8");
		
		// JSP needs a phonebook. It is possible to create an exemplar of the phonebook in JSP.
		// But from an architectural point of view it is more logical to create the exemplar in a servlet and transmit the exemplar to JSP. 
		request.setAttribute("phonebook", this.phonebook);
		
		// repository of parameters to transfer to JSP
		HashMap<String,String> jsp_parameters = new HashMap<String,String>();

		// dispatchers to transmit the control to different JSPs (different views)
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
        RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
        RequestDispatcher dispatcher_for_new_manager = request.getRequestDispatcher("/ManageNewPerson.jsp");
        

        // the id of a record and an action to be done at the record
		String action = request.getParameter("action");
		String id = request.getParameter("id");
		
		// it is a state just to show the list and not to do anything if any id and action are not indicated 
        if ((action == null)&&(id == null))
        {
        	request.setAttribute("jsp_parameters", jsp_parameters);
            dispatcher_for_list.forward(request, response);
        }
        // if any action is indicated...
        else
        {
        	switch (action)
        	{
        		// addition of a record
        		case "add":
        			// creation of a new empty user's record
        			Person empty_person = new Person();
        			
        			// preparing parameters for JSP
        			jsp_parameters.put("current_action", "add");
        			jsp_parameters.put("next_action", "add_go");
        			jsp_parameters.put("next_action_label", "Add");
        			
        			// setting the parameters of JSP
        			request.setAttribute("person", empty_person);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// the transfer of the request to JSP
        			dispatcher_for_new_manager.forward(request, response);
        		break;
			
        		case "edit":        			
        			Person editable_person = this.phonebook.getPerson(id);
        			
        			jsp_parameters.put("current_action", "edit");
        			jsp_parameters.put("next_action", "edit_go");
        			jsp_parameters.put("next_action_label", "Save");

        			request.setAttribute("person", editable_person);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			dispatcher_for_manager.forward(request, response);
        		break;
			
        		case "delete":
        			
        			if (phonebook.deletePerson(id))
        			{
        				jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
        				jsp_parameters.put("current_action_result_label", "The deletion succeeded.");
        			}
        			else
        			{
        				jsp_parameters.put("current_action_result", "DELETION_FAILURE");
        				jsp_parameters.put("current_action_result_label", "The deletion failed (maybe, the record was not found)!");
        			}

        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			dispatcher_for_list.forward(request, response);
       			break;
       		}
        }
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		request.setCharacterEncoding("UTF-8");

		request.setAttribute("phonebook", this.phonebook);
		
		HashMap<String,String> jsp_parameters = new HashMap<String,String>();

		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		
		
		String add_go = request.getParameter("add_go");
		String edit_go = request.getParameter("edit_go");
		String id = request.getParameter("id");
		
		if (add_go != null)
		{
			Person new_person = new Person(request.getParameter("name"), request.getParameter("surname"), request.getParameter("middlename"));

			String error_message = this.validatePersonFMLName(new_person); 
			
			if (error_message.equals(""))
			{

				if (this.phonebook.addPerson(new_person))
				{
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "The addition succeeded.");
				}
				else
				{
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "The addition failed!");
				}

				request.setAttribute("jsp_parameters", jsp_parameters);
	        
				dispatcher_for_list.forward(request, response);
			}
			else
			{
    			jsp_parameters.put("current_action", "add");
    			jsp_parameters.put("next_action", "add_go");
    			jsp_parameters.put("next_action_label", "Add");
    			jsp_parameters.put("error_message", error_message);
    			
    			request.setAttribute("person", new_person);
    			request.setAttribute("jsp_parameters", jsp_parameters);
    			
    			dispatcher_for_manager.forward(request, response);
			}
		}
		
		if (edit_go != null)
		{
			Person updatable_person = this.phonebook.getPerson(request.getParameter("id")); 
			updatable_person.setName(request.getParameter("name"));
			updatable_person.setSurname(request.getParameter("surname"));
			updatable_person.setMiddlename(request.getParameter("middlename"));

			String error_message = this.validatePersonFMLName(updatable_person); 
			
			if (error_message.equals(""))
			{
			
				if (this.phonebook.updatePerson(id, updatable_person))
				{
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "The update succeeded.");
				}
				else
				{
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "The update failed!");
				}

				request.setAttribute("jsp_parameters", jsp_parameters);
	        
				dispatcher_for_list.forward(request, response);
			}
			else
			{
    			jsp_parameters.put("current_action", "edit");
    			jsp_parameters.put("next_action", "edit_go");
    			jsp_parameters.put("next_action_label", "Save");
    			jsp_parameters.put("error_message", error_message);

    			request.setAttribute("person", updatable_person);
    			request.setAttribute("jsp_parameters", jsp_parameters);
    			
    			dispatcher_for_manager.forward(request, response);    			
    			
			}
		}
	}
}
