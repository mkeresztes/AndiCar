	( TASK_TODO.DueMileage - DEF_CAR.IndexCurrent)  
	/ 
	(
		( DEF_CAR.IndexCurrent - Minimums.Mileage )  
		/
		(strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) 
	),	
	COALESCE(strftime('%J', datetime(TASK_TODO.DueDate, 'unixepoch'), 'localtime'), 0)  -  strftime('%J','now', 'localtime')




SELECT 
  TASK_TODO._id, DEF_TASK.Name, 
        CASE  
            WHEN DEF_TASK.ScheduledFor = 'B'  
                        AND Minimums.Mileage IS NOT NULL AND Minimums.Date IS NOT NULL  
                AND ( ( ( TASK_TODO.DueMileage - DEF_CAR.IndexCurrent)  / (( DEF_CAR.IndexCurrent - Minimums.Mileage )  /  (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) ) )  < (  COALESCE(strftime('%J', datetime(TASK_TODO.DueDate, 'unixepoch'), 'localtime'), 0)  -  strftime('%J','now', 'localtime')  )  )
                        THEN ( ( TASK_TODO.DueMileage - DEF_CAR.IndexCurrent)  / (( DEF_CAR.IndexCurrent - Minimums.Mileage )  /  (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) ) ) 
            WHEN (DEF_TASK.ScheduledFor = 'B'  OR DEF_TASK.ScheduledFor = 'M') 
                        AND  Minimums.Mileage IS NULL  
                        THEN 99999999999  
                WHEN DEF_TASK.ScheduledFor = 'M'  
                        AND Minimums.Mileage IS NOT NULL  
                                THEN ( ( TASK_TODO.DueMileage - DEF_CAR.IndexCurrent)  / (( DEF_CAR.IndexCurrent - Minimums.Mileage )  /  (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) ) )
                WHEN DEF_TASK.ScheduledFor = 'T'  
                                THEN (  COALESCE(strftime('%J', datetime(TASK_TODO.DueDate, 'unixepoch'), 'localtime'), 0)  -  strftime('%J','now', 'localtime')  )
                ELSE 222
        END AS EstDueDays,
        DEF_TASK.ScheduledFor,
        Minimums.Mileage,
        Minimums.Date,
		( TASK_TODO.DueMileage - DEF_CAR.IndexCurrent) / 
		(
			( DEF_CAR.IndexCurrent - Minimums.Mileage )  / (strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) ) 
		),	
		COALESCE(strftime('%J', datetime(TASK_TODO.DueDate, 'unixepoch'), 'localtime'), 0)  -  strftime('%J','now', 'localtime')

FROM TASK_TODO 
        JOIN DEF_TASK ON TASK_TODO.DEF_TASK_ID = DEF_TASK._id 
                JOIN DEF_TASKTYPE ON DEF_TASK.DEF_TASKTYPE_ID = DEF_TASKTYPE._id 
        LEFT OUTER JOIN DEF_CAR ON TASK_TODO.DEF_CAR_ID = DEF_CAR._id 
                LEFT OUTER JOIN DEF_UOM ON DEF_CAR.DEF_UOM_Length_ID = DEF_UOM._id 
        LEFT OUTER JOIN 
                (SELECT MIN(Date) AS Date, MIN(Mileage) AS Mileage, CAR_ID
                FROM  
                        (SELECT MIN(Date) AS Date,  MIN(IndexStart) AS Mileage, DEF_CAR_ID AS CAR_ID
                        FROM CAR_MILEAGE
                        WHERE IsActive = 'Y'
                        GROUP BY DEF_CAR_ID
                        UNION
                        SELECT MIN(Date) AS Date,  MIN(CarIndex) AS Mileage, DEF_CAR_ID AS CAR_ID
                        FROM CAR_REFUEL
                        WHERE IsActive = 'Y'
                        GROUP BY DEF_CAR_ID
                        UNION  SELECT MIN(Date) AS Date,  MIN(CarIndex) AS Mileage, DEF_CAR_ID AS CAR_ID
                        FROM CAR_EXPENSE
                        WHERE IsActive = 'Y'
                        GROUP BY DEF_CAR_ID)
                GROUP BY CAR_ID) AS Minimums ON Minimums.CAR_ID = TASK_TODO.DEF_CAR_ID 
ORDER BY EstDueDays ASC, COALESCE (TASK_TODO.DueMileage, 0) ASC