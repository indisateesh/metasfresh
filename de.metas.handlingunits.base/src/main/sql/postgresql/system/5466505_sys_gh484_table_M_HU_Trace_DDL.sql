
-- 2017-06-29T12:13:55.778
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
/* DDL */ CREATE TABLE public.M_HU_Trace (AD_Client_ID NUMERIC(10) NOT NULL, AD_Org_ID NUMERIC(10) NOT NULL, Created TIMESTAMP WITH TIME ZONE NOT NULL, CreatedBy NUMERIC(10) NOT NULL, IsActive CHAR(1) CHECK (IsActive IN ('Y','N')) NOT NULL, M_HU_ID NUMERIC(10) NOT NULL, M_HU_Source_ID NUMERIC(10), M_HU_Trace_ID NUMERIC(10) NOT NULL, M_InOut_ID NUMERIC(10), M_Movement_ID NUMERIC(10), M_ShipmentSchedule_ID NUMERIC(10), PP_Order_ID NUMERIC(10), Updated TIMESTAMP WITH TIME ZONE NOT NULL, UpdatedBy NUMERIC(10) NOT NULL, CONSTRAINT MHU_MHUTrace FOREIGN KEY (M_HU_ID) REFERENCES public.M_HU DEFERRABLE INITIALLY DEFERRED, CONSTRAINT MHUSource_MHUTrace FOREIGN KEY (M_HU_Source_ID) REFERENCES public.M_HU DEFERRABLE INITIALLY DEFERRED, CONSTRAINT M_HU_Trace_Key PRIMARY KEY (M_HU_Trace_ID), CONSTRAINT MInOut_MHUTrace FOREIGN KEY (M_InOut_ID) REFERENCES public.M_InOut DEFERRABLE INITIALLY DEFERRED, CONSTRAINT MMovement_MHUTrace FOREIGN KEY (M_Movement_ID) REFERENCES public.M_Movement DEFERRABLE INITIALLY DEFERRED, CONSTRAINT MShipmentSchedule_MHUTrace FOREIGN KEY (M_ShipmentSchedule_ID) REFERENCES public.M_ShipmentSchedule DEFERRABLE INITIALLY DEFERRED, CONSTRAINT PPOrder_MHUTrace FOREIGN KEY (PP_Order_ID) REFERENCES public.PP_Order DEFERRABLE INITIALLY DEFERRED)
;

-- 2017-06-29T19:17:47.341
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
/* DDL */ SELECT public.db_alter_table('m_hu_trace','ALTER TABLE public.M_HU_Trace ADD COLUMN EventTime TIMESTAMP WITH TIME ZONE')
;

