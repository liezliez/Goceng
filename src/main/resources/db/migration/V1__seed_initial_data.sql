-- Insert roles
INSERT INTO roles (role_name) VALUES
  ('ROLE_SUPERADMIN'),
  ('ROLE_CUSTOMER'),
  ('ROLE_MARKETING'),
  ('ROLE_BRANCH_MANAGER'),
  ('ROLE_BACK_OFFICE');

INSERT INTO plafon (id,interest_rate,plafon_amount,plafon_type) VALUES
	 (N'9C38D7E0-5C7E-4A58-A527-0D11F4B6A6A1',0.05,5000000.00,N'BRONZE'),
	 (N'4C8C4A5F-BF77-41E5-9F4F-67A44A35D0B3',0.04,8000000.00,N'SILVER'),
	 (N'E3F3C5A1-8C32-41D0-9B72-84F2F9D57C88',0.03,15000000.00,N'GOLD');


-- Insert branches
INSERT INTO branch (id_branch, name, address, city, province, latitude, longitude) VALUES
  ('42F47C49-01B9-423A-AA56-D160F8196641', 'Jakarta Pusat', 'Jl. Merdeka No.1', 'Jakarta', 'DKI Jakarta', -6.2088, 106.8456),
  ('B43A94D7-4C5E-4F2D-8A7B-02477F36D65F', 'Bandung', 'Jl. Asia Afrika No.22', 'Bandung', 'Jawa Barat', -6.9175, 107.6191),
  ('31C93F27-70A9-44A0-A922-D0BC906A47A1', 'Surabaya', 'Jl. Pemuda No.17', 'Surabaya', 'Jawa Timur', -7.2504, 112.7688),
  ('9B9A08D0-67C7-4783-B5F0-35E0E1A3D0D0', 'Medan', 'Jl. Gatot Subroto No.5', 'Medan', 'Sumatera Utara', 3.5952, 98.6722),
  ('F4A8DA3E-0458-4694-B8F3-1D17E7B26937', 'Yogyakarta', 'Jl. Malioboro No.11', 'Yogyakarta', 'DI Yogyakarta', -7.7956, 110.3695),
  ('84936B2B-960F-41A0-BE4B-B3C76D8A1705', 'Makassar', 'Jl. Sudirman No.9', 'Makassar', 'Sulawesi Selatan', -5.1477, 119.4328),
  ('A4C56E9D-07F2-4D6D-B6F1-5A4F6DC5975D', 'Denpasar', 'Jl. Teuku Umar No.6', 'Denpasar', 'Bali', -8.4095, 115.1889),
  ('D699DB1D-F8E4-42B9-974E-F256E5F05339', 'Semarang', 'Jl. Pandanaran No.3', 'Semarang', 'Jawa Tengah', -6.9669, 110.4194),
  ('FD7B1328-60D9-44B3-9E93-0C2D126BC6EF', 'Balikpapan', 'Jl. Jendral Sudirman No.2', 'Balikpapan', 'Kalimantan Timur', -1.2654, 116.9026),
  ('B8828AE4-2266-4634-BB2A-4B8C870B32F5', 'Padang', 'Jl. Ahmad Yani No.7', 'Padang', 'Sumatera Barat', -0.9470, 100.4170);

INSERT INTO users (id,account_status,email,fcmtoken,name,password,id_branch,id_role) VALUES
	 (N'72A54A2B-6F4F-48F4-BFE7-000DF677C706',N'ACTIVE',N'customer@example.org',N'dx4ycamHRWatyK4O7mx64X:APA91bHchjcA2dJzTikUTazGRgKsvIBtfPq93hSwlQ_uUx8Y8eoNgTYyZ_Qj72ePE3LrEmAHjobFhc_H8VriMv7dcqRc5cMA_A902ea6wzkcd5EZtBqJf9Q',N'Customer',N'$2a$10$2Sz6mkMOfi31bRHVBFuGTOSJAWWe3E/wQgkR8m185J0NKgiYPMoj.',N'42F47C49-01B9-423A-AA56-D160F8196641',2),
	 (N'E26A82D4-4F5D-4226-8593-19251B776277',N'ACTIVE',N'aisyah@example.org',N'dx4ycamHRWatyK4O7mx64X:APA91bHchjcA2dJzTikUTazGRgKsvIBtfPq93hSwlQ_uUx8Y8eoNgTYyZ_Qj72ePE3LrEmAHjobFhc_H8VriMv7dcqRc5cMA_A902ea6wzkcd5EZtBqJf9Q',N'Aisyah',N'$2a$10$hKiT.zL0jHE2Q/vkz5ULiuz1IUtE9AewnUi3bpmj5m8.rb5E5uEP6',N'42F47C49-01B9-423A-AA56-D160F8196641',2),
	 (N'7E008E63-4B4F-4CF7-8E41-813593066A7A',N'ACTIVE',N'superadmin@example.org',NULL,N'Superadmin',N'$2a$10$lWw4y4z9aYLIcAMfJzSaeOKc2D8euLhTo5Jub6fOyOMTnum8PkIRy',N'42F47C49-01B9-423A-AA56-D160F8196641',1),
	 (N'2A733039-184A-4B75-A92E-9ED920481C12',N'ACTIVE',N'marketing@example.org',NULL,N'Marketing',N'$2a$10$PULiXRDwgBxIRvePMqMfcu3chjEodn4evV7S2PzMictKiEGciAu2e',N'42F47C49-01B9-423A-AA56-D160F8196641',3),
	 (N'6E8A1D43-BDA8-4E45-8655-D276AD0CF0C0',N'ACTIVE',N'bm@example.org',NULL,N'Branch Manager',N'$2a$10$Nj/izbUcFCXiUzxWf2FFL.oQoU7kpx.hdOBGyna8eNhWXGJdkCJiO',N'42F47C49-01B9-423A-AA56-D160F8196641',4),
	 (N'2871926D-1E9C-4E3A-9915-F5A66C2EBC25',N'ACTIVE',N'bo@example.org',NULL,N'Back Office',N'$2a$10$db8Q10H2o5NgYoMFWI4/SeRYz2Y/DYcPOZOS6dZ4XbSkX2TjQdtTe',N'42F47C49-01B9-423A-AA56-D160F8196641',5);

INSERT INTO customers (id,account_no,address,credit_limit,date_of_birth,emergency_call,home_ownership_status,mother_maiden_name,name,nik,occupation,place_of_birth,salary,telp_no,url_ktp,url_selfie,id_plafon,id_user) VALUES
	 (N'41C7C78A-DC33-4DEF-89AD-11BB5522A876',NULL,NULL,5000000.00,NULL,NULL,NULL,NULL,N'Aisyah',N'6555598989',NULL,NULL,NULL,NULL,NULL,NULL,N'9C38D7E0-5C7E-4A58-A527-0D11F4B6A6A1',N'E26A82D4-4F5D-4226-8593-19251B776277'),
	 (N'2BE4BAF2-AB97-4CC0-A747-2279E7D8F288',N'65789997',N'bskskmdmdd',5000000.00,'2025-05-21',N'0897648431',N'Punya snediri',N'wati',N'Arda',N'32554646464',N'software engineer',N'baturaja',50000000.00,N'084645434',N'https://res.cloudinary.com/driygaq4s/image/upload/v1748104240/v0mv0drouiqxdkswx5vo.jpg',N'https://res.cloudinary.com/driygaq4s/image/upload/v1748104245/peobxkxlkhwi004n6qul.jpg',N'9C38D7E0-5C7E-4A58-A527-0D11F4B6A6A1',N'72A54A2B-6F4F-48F4-BFE7-000DF677C706');

 INSERT INTO employees (id,nip,name,version,work_status,id_branch,id_user) VALUES
	 (N'D0C53754-7B37-41D7-9378-1F8CC72E645E',N'2025BO001',N'Taurean.Wyman91',0,N'ACTIVE',N'42F47C49-01B9-423A-AA56-D160F8196641',N'2871926D-1E9C-4E3A-9915-F5A66C2EBC25'),
	 (N'58B4B954-2A35-4301-84FE-ACD561D8BA0F',N'2025MK001',N'Blaze.Brekke31',0,N'ACTIVE',N'42F47C49-01B9-423A-AA56-D160F8196641',N'2A733039-184A-4B75-A92E-9ED920481C12'),
	 (N'4AE969C1-6134-446E-B39E-F100C50F1E05',N'2025BM001',N'Serena.Gutkowski',0,N'ACTIVE',N'42F47C49-01B9-423A-AA56-D160F8196641',N'6E8A1D43-BDA8-4E45-8655-D276AD0CF0C0');

INSERT INTO application (id,amount,back_office_assigned_time,back_office_note,branch_manager_assigned_time,branch_manager_note,created_at,interest_rate,marketing_assigned_time,marketing_note,nip_back_office,nip_branch_manager,nip_marketing,plafon_limit,plafon_type,purpose,status,tenor,updated_at,id_back_office_assigned,id_branch,id_branch_manager_assigned,id_customer,id_marketing_assigned,id_plafon) VALUES
	 (N'BE6DF978-6BDC-4D47-B170-4B39783BD749',680000.00,NULL,NULL,NULL,NULL,'2025-05-28 06:15:44.808',0.05,'2025-05-28 06:27:27.760',N'Bagus nih bos',NULL,NULL,N'2025MK001',5000000.00,N'BRONZE',N'Kado anak',N'PENDING_BRANCH_MANAGER',12,'2025-05-28 06:27:27.774',NULL,N'42F47C49-01B9-423A-AA56-D160F8196641',NULL,N'2BE4BAF2-AB97-4CC0-A747-2279E7D8F288',N'2A733039-184A-4B75-A92E-9ED920481C12',N'9C38D7E0-5C7E-4A58-A527-0D11F4B6A6A1');


-- Insert features
INSERT INTO features (feature_name) VALUES
  ('VIEW_DASHBOARD'),
  ('CREATE_USER'),
  ('APPROVE_APPLICATION'),
  ('MANAGE_USERS'),
  ('MANAGE_BRANCHES'),
  ('CHANGE_PASSWORD');

-- Assign features to roles
-- SUPERADMIN gets all features
INSERT INTO role_features (id_role, id_feature)
SELECT r.id, f.id FROM roles r CROSS JOIN features f WHERE r.role_name = 'ROLE_SUPERADMIN';

-- MARKETING gets selected features
INSERT INTO role_features (id_role, id_feature)
SELECT r.id, f.id FROM roles r, features f WHERE r.role_name = 'ROLE_MARKETING' AND f.feature_name IN (
  'VIEW_DASHBOARD',
  'APPROVE_APPLICATION',
  'CHANGE_PASSWORD'
);

-- BRANCH_MANAGER gets selected features
INSERT INTO role_features (id_role, id_feature)
SELECT r.id, f.id FROM roles r, features f WHERE r.role_name = 'ROLE_BRANCH_MANAGER' AND f.feature_name IN (
  'VIEW_DASHBOARD',
  'APPROVE_APPLICATION',
  'CHANGE_PASSWORD'
);

-- BACK_OFFICE gets selected features
INSERT INTO role_features (id_role, id_feature)
SELECT r.id, f.id FROM roles r, features f WHERE r.role_name = 'ROLE_BACK_OFFICE' AND f.feature_name IN (
  'VIEW_USER',
  'VIEW_DASHBOARD',
  'APPROVE_APPLICATION',
  'CHANGE_PASSWORD'
);
