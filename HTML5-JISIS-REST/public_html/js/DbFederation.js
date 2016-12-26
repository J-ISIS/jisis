/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




$(document).ready(function() {
   
  // ==================
   console.log('DbFederation');
   $.ajax({
      type: 'GET',
      url: "http://localhost:8084/J-ISIS-REST",
      dataType: "json",
      success: function(data) {
         alert('loaded ' + this.url);
         var s = JSON.stringify(data);
       
         console.log(s);
         arrayOfDbReferences = JSON.parse(s);

         for (var i = 0; i < arrayOfDbReferences.length; i++) 
         {
            console.log(arrayOfDbReferences[i]);
            var dbHome = arrayOfDbReferences[i].dbHome_;
            var dbName = arrayOfDbReferences[i].dbName_;
            //alert('dbHome=' + dbHome + ' dbName=' + dbName);
            $('.DbFederation-content').append('<tr><td> '+dbHome+' </td> <td> '+dbName+' </td></tr>');
                  
         }

      }
   });
  // ===================
  
});
       

                


