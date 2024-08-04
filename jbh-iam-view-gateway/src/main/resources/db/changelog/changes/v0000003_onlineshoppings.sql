CREATE TABLE online_shopping (
   id UUID default gen_random_uuid() not null,
   user_group_id UUID,
   effective_date date not null,
   articule_name varchar(50),
   grand_total numeric(16,2),
   usd_price numeric(10,2),
   zone_id_price numeric(10,2),
   dollar_rate numeric(6,2),
   shipping_weight numeric(5,4),
   shipping_price numeric(10,2),
   created_at timestamp with time zone  not null,
   updated_at timestamp with time zone ,
 primary key (id)
);

alter table if exists online_shopping
  add constraint FK_online_shoppings_by_user_group_id
    foreign key (user_group_id)
      references users_group;

