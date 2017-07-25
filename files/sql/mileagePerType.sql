select  
			strftime('%Y', CAR_MILEAGE.Date, 'unixepoch', 'localtime') AS Year,
			strftime('%m', CAR_MILEAGE.Date, 'unixepoch', 'localtime') AS Month,
			DEF_CAR.Name, 
   		DEF_EXPENSETYPE.Name,
			SUM(CAR_MILEAGE.IndexStop - CAR_MILEAGE.IndexStart) AS Mileage
from CAR_MILEAGE
			JOIN DEF_EXPENSETYPE ON (CAR_MILEAGE.DEF_EXPENSETYPE_ID = DEF_EXPENSETYPE._id)
			JOIN DEF_CAR ON (CAR_MILEAGE.DEF_CAR_ID = DEF_CAR._id)
group by  1, 2, 3, 4

--DEF_EXPENSETYPE.Name, DEF_CAR.Name;

select DATETIME( CAR_MILEAGE.Date, 'unixepoch', 'localtime'), strftime('%m', CAR_MILEAGE.Date, 'unixepoch', 'localtime') 
from CAR_MILEAGE
