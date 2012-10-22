
function editlink(to,newwin)
{
     if (newwin)
		{
		window.open('/cgi-bin/edit.cgi?pname=' + to, '');
		}
		else
		{ location.href='/cgi-bin/edit.cgi?pname=' + to;
		}
}

  //
    // Check registation for information
    //
function isFieldBlank(theField,which) 
{
	//alert("Checking Lenth of Field:" +which );
        return( theField.value.length == 0);
}

function isFieldValid(theField) 
{
	var valid = "abcdefghijklmnopqrstuvwxyz0123456789";
	var len = theField.value.length;
	if ( (len < 3) || (len > 10) ) {
		return false;
	}
	//alert("Checking field of length " + theField.value.length );
	for( i=0; i<theField.value.length; i++ ) 
	{
		c = theField.value.substring(i, i+1).toLowerCase();
		//alert( "Checking " + c );
		if( valid.indexOf(c, 0) == -1 ) {
			//alert( "Invalid " + c );
			return false;
		}
	}
        return true;
}
    //
    // Check for a valid email address (Does it contain a "@")
    //
function isValidEmail(theField) 
{
      var foundSymbol = false;
      var len = theField.value.length;
      if (theField.value.substring(0,1) == "@") 
      {   return false;
      }
      if (theField.value.substring(len-1,len) == "@") 
      {
                   return false;
      }
      for(var i=1; i<theField.value.length; i++) 
      {
       var ch = theField.value.substring(i,i+1);
       if (ch == "@")
       foundSymbol=true;
      }
      return foundSymbol;
}

function validateForm(form1) 
{
  if(isFieldBlank(form1.fullname,"fullname")) 
  {
       alert("\nThe full name is empty.");
       return false;
  }
  if(isFieldBlank(form1.pname,"pname")) 
  {
       alert("\nThe player name is empty.");
       return false;
  }
  if(isFieldBlank(form1.password,"password"))
  {  alert("\nThe password is blank");
      return false;
  }
  if(!(form1.password.value == form1.password2.value))
  { alert("\nPassword and Password2 don't match");
  return false;
  }
    if(!isFieldValid(form1.pname)) 
  {
            alert("\nThe player name is invalid.\n\nThe name must be between 3 and 10 characters and may only contain characters a-z, A-Z and 0-9.  ");
            return false;
  }
    if(isFieldBlank(form1.email,"email")) 
  {
            alert("\nThe email address is empty.");
            return false;
  }
    if(!isValidEmail(form1.email)) 
  {
            alert("\nInvalid email address.");
            return false;
  }

    return true;
}

function checkRegisterForm(form2) 
{
	//alert(form2);
  if( validateForm( form2 ) ) 
	{
        form2.submit();
        return true;
        } 
  else 
    {
    return false;
     }

}
function validateEditForm(form1) 
{
  if(isFieldBlank(form1.fullname,"fullname")) 
  {
       alert("\nThe full name is empty.");
       return false;
  }
  if(!(form1.newpasswd.value == form1.newpasswd2.value))
  { alert("\nNewPassword and NewPassword2 don't match");
  return false;
  }
    if(!isFieldBlank(form1.newpname,"newpname") && !isFieldValid(form1.newpname)) 
  {
            alert("\nThe player name is invalid.\n\nThe name must be between 3 and 10 characters and may only contain characters a-z, A-Z and 0-9.  ");
            return false;
  }
    if(isFieldBlank(form1.email,"email")) 
  {
            alert("\nThe email address is empty.");
            return false;
  }
    if(!isValidEmail(form1.email)) 
  {
            alert("\nInvalid email address.");
            return false;
  }

    return true;
}
function checkEditForm(form2) 
{
  //alert(form2);
  if( validateEditForm( form2 ) ) 
  {
        form2.submit();
        return true;
        } 
  else 
    {
    return false;
     }

}

function hexEncode(data){
	var b16_digits = '0123456789abcdef';
	var b16_map = new Array();
	for (var i=0; i<256; i++) {
		b16_map[i] = b16_digits.charAt(i >> 4) + b16_digits.charAt(i & 15);
	}
	
	var result = new Array();
	for (var i=0; i<data.length; i++) {
		result[i] = b16_map[data.charCodeAt(i)];
	}
	
	return result.join('');
}

//Decodes Hex(base16) formated data
function hexDecode(data){
	var b16_digits = '0123456789abcdef';
	var b16_map = new Array();
	for (var i=0; i<256; i++) {
		b16_map[b16_digits.charAt(i >> 4) + b16_digits.charAt(i & 15)] = String.fromCharCode(i);
	}
	if (!data.match(/^[a-f0-9]*$/i)) return false;// return false if input data is not a valid Hex string
	
	if (data.length % 2) data = '0'+data;
		
	var result = new Array();
	var j=0;
	for (var i=0; i<data.length; i+=2) {
		result[j++] = b16_map[data.substr(i,2)];
	}

	return result.join('');
}

function obPrint(str2,str1)
{	var combo = str1 + str2;
	var str3 = hexDecode(combo);
	document.write(str3);
}

// facebook "like" code
<div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/en_US/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>
