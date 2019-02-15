DROP FUNCTION IF EXISTS de_metas_endcustomer_fresh_reports.direct_costing_raw_data(double precision);
DROP FUNCTION IF EXISTS de_metas_endcustomer_fresh_reports.Direct_Costing_Raw_Data (Year Date) ;
CREATE OR REPLACE FUNCTION de_metas_endcustomer_fresh_reports.Direct_Costing_Raw_Data (Year Date) RETURNS TABLE
	(
	Margin text, 
	l1_Value Character Varying, L1_Name Character Varying, L2_Value Character Varying, L2_Name Character Varying, L3_Value Character Varying, L3_Name Character Varying, 
	Balance_1000 numeric, Balance_2000 numeric, Balance_100 numeric, Balance_150 numeric, Balance_Other numeric, Balance numeric, 
	Budget_1000 numeric, Budget_2000 numeric, Budget_100 numeric, Budget_150 numeric, Budget numeric, 
	L1_Multiplicator numeric, L2_Multiplicator numeric, L3_Multiplicator numeric, Seq text
	)
AS 
$BODY$
SELECT
	margin, 
	l1_value, l1_name, l2_value, l2_name, l3_value, l3_name,
	Balance_1000 * Multi_1000 AS Balance_1000,
	Balance_2000 * Multi_2000 AS Balance_2000,
	Balance_100 * Multi_100 AS Balance_100,
	Balance_150 * Multi_150 AS Balance_150,
	Balance_Other * Multi_150 AS Balance_Other,
	Balance_1000 * Multi_1000 + Balance_2000 * Multi_2000 + Balance_100 * Multi_100 + Balance_150 * Multi_150 AS Balance,
	Budget_1000 * Multi_1000 AS Budget_1000,
	Budget_2000 * Multi_2000 AS Budget_2000,
	Budget_100 * Multi_100 AS Budget_100,
	Budget_150 * Multi_150 AS Budget_150,
	Budget_1000 * Multi_1000 + Budget_2000 * Multi_2000 + Budget_100 * Multi_100 + Budget_150 * Multi_150 AS Budget,
	acctBalance(l3_ElementValue_ID, 0, 1) AS l3_Multiplicator,
	acctBalance(l2_ElementValue_ID, 0, 1) AS l2_Multiplicator,
	acctBalance(l1_ElementValue_ID, 0, 1) AS l1_Multiplicator,
	SeqNo AS Seq
FROM
	de_metas_endcustomer_fresh_reports.Direct_Costing_selection s
	LEFT OUTER JOIN (
		SELECT
			Account_ID,
			SUM( CASE WHEN a.Value = '1000' THEN Balance ELSE 0 END ) AS Balance_1000,
			SUM( CASE WHEN a.Value = '2000' THEN Balance ELSE 0 END ) AS Balance_2000,
			SUM( CASE WHEN a.Value = '100' THEN Balance ELSE 0 END ) AS Balance_100,
			SUM( CASE WHEN a.Value = '150' THEN Balance ELSE 0 END ) AS Balance_150,
			SUM( CASE WHEN a.Value IS NULL OR (a.Value != '1000' AND a.Value != '2000' AND a.Value != '100' AND a.Value != '150') 
				THEN Balance ELSE 0 END ) AS Balance_Other,
			
			SUM( CASE WHEN a.Value = '1000' THEN Budget ELSE 0 END ) AS Budget_1000,
			SUM( CASE WHEN a.Value = '2000' THEN Budget ELSE 0 END ) AS Budget_2000,
			SUM( CASE WHEN a.Value = '100' THEN Budget ELSE 0 END ) AS Budget_100,
			SUM( CASE WHEN a.Value = '150' THEN Budget  ELSE 0 END ) AS Budget_150
		FROM
			(
				SELECT Account_ID, C_Activity_ID, 
					CASE WHEN postingtype = 'A' THEN AmtAcctCr - AmtAcctDr ELSE 0 END AS Balance,
					CASE WHEN postingtype = 'B' THEN AmtAcctCr - AmtAcctDr ELSE 0 END AS Budget
				FROM 	Fact_Acct 
				WHERE	dateacct::date <= $1
					AND dateacct::Date >= (
						SELECT MIN( StartDate )::Date FROM C_Period 
						WHERE C_Year_ID = (SELECT C_Year_ID FROM C_Period WHERE C_Period_ID = report.Get_Period( 1000000, $1 ))
					)
			) fa
			LEFT OUTER JOIN C_Activity a ON fa.C_Activity_ID = a.C_Activity_ID 
		GROUP BY Account_ID
	) fa ON fa.Account_ID = s.L3_ElementValue_ID
ORDER BY
	SeqNo
$BODY$
LANGUAGE sql STABLE
;

